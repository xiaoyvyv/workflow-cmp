package com.xiaoyv.workflow.platform.room.cookie

import com.xiaoyv.workflow.platform.room.WorkflowRoomDatabase
import com.xiaoyv.workflow.platform.room.WorkflowRoomDatabaseFactory
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 基于 Room（SQLite）持久化的 Ktor [CookiesStorage] 生产级专业实现。
 *
 * 特性：
 * 1. **RFC 6265 标准匹配**：严格支持 Domain（含子域匹配）、Path 层级匹配、Secure 传输协议匹配；
 * 2. **生命周期与自动清理**：精确计算 `maxAge` 与 `expires`，每次读写时触发过期清理；
 * 3. **高性能结构化存储**：每条 Cookie 独立入库并建立复合索引（Domain、Path、Expires），避免全量重写开销；
 * 4. **并发安全保证**：通过 [Mutex] 保证读写原子性与一致性；
 * 5. **丰富的管理接口**：提供获取全部、按条件清理、批量导入/覆盖等完备管理能力。
 */
class RoomActionCookiesStorage(
    private val database: WorkflowRoomDatabase = defaultDatabase,
) : CookiesStorage {

    private val dao = database.cookieDao()
    private val mutex = Mutex()

    /**
     * 获取匹配指定请求 URL 的所有有效 Cookie，按 Path 长度降序排序。
     */
    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = GMTDate().timestamp
        // 自动清理过期数据
        dao.deleteExpiredCookies(now)

        // 查询所有 Cookie 并匹配当前请求 URL
        val entities = dao.getAllCookies()
        entities.filter { entity ->
            !entity.isExpired(now) && entity.toCookie().matches(requestUrl)
        }.map { it.toCookie() }
            .sortedByDescending { it.path?.length ?: 0 }
    }

    /**
     * 写入或更新来自指定请求 URL 响应的 Cookie。
     */
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        val now = GMTDate().timestamp
        val normalized = cookie.copy(path = cookie.path ?: "/").fillDefaults(requestUrl)
        val entity = RoomActionCookieEntity.fromCookie(normalized, createdAt = now)

        // 先删除旧条目（相同 name, domain, path）
        dao.deleteCookie(
            name = entity.name,
            domain = entity.domain,
            path = entity.path,
        )

        // 如果不是删除指令且未过期，则写入新条目
        if (!entity.isExpired(now)) {
            dao.insertOrUpdate(entity)
        }

        // 清理已过期的记录
        dao.deleteExpiredCookies(now)
    }

    /**
     * 获取所有当前保存且未过期的 Cookie 列表。
     */
    suspend fun getAll(): List<Cookie> = mutex.withLock {
        val now = GMTDate().timestamp
        dao.deleteExpiredCookies(now)
        dao.getAllCookies().filter { !it.isExpired(now) }.map { it.toCookie() }
    }

    /**
     * 批量导入或更新 Cookie 列表。
     */
    suspend fun setCookies(newCookies: List<Cookie>): Unit = mutex.withLock {
        val now = GMTDate().timestamp
        for (newCookie in newCookies) {
            val entity = RoomActionCookieEntity.fromCookie(newCookie, createdAt = now)
            dao.deleteCookie(
                name = entity.name,
                domain = entity.domain,
                path = entity.path,
            )
            if (!entity.isExpired(now)) {
                dao.insertOrUpdate(entity)
            }
        }
        dao.deleteExpiredCookies(now)
    }

    /**
     * 按条件删除指定 Cookie（例如清除特定域名下的全部 Cookie）。
     */
    suspend fun remove(predicate: (Cookie) -> Boolean): Unit = mutex.withLock {
        val entities = dao.getAllCookies()
        for (entity in entities) {
            if (predicate(entity.toCookie())) {
                dao.deleteById(entity.id)
            }
        }
    }

    /**
     * 清空所有已保存的 Cookie。
     */
    suspend fun clear(): Unit = mutex.withLock {
        dao.clearAll()
    }

    override fun close() {
        database.close()
    }

    companion object {
        private val defaultDatabase by lazy {
            WorkflowRoomDatabaseFactory.createDatabase()
        }
    }
}
