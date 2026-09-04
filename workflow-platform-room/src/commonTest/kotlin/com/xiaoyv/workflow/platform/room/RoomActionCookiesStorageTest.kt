package com.xiaoyv.workflow.platform.room

import com.xiaoyv.workflow.platform.room.cookie.RoomActionCookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.SYSTEM
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoomActionCookiesStorageTest {

    private val testDbPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test_cookies_${Random.nextLong().toString(16)}.db"
    private val database = WorkflowRoomDatabaseFactory.createDatabase(testDbPath.toString())
    private val storage = RoomActionCookiesStorage(database)

    @AfterTest
    fun tearDown() {
        database.close()
        runCatching {
            FileSystem.SYSTEM.delete(testDbPath)
            FileSystem.SYSTEM.delete(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${testDbPath.name}-wal")
            FileSystem.SYSTEM.delete(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "${testDbPath.name}-shm")
        }
    }


    @Test
    fun testBasicAddAndGetMatchingUrl() = runBlocking {
        val targetUrl = Url("https://example.com/api/users")
        val cookie = Cookie(
            name = "token",
            value = "abc123xyz",
            domain = "example.com",
            path = "/api",
        )

        storage.addCookie(targetUrl, cookie)

        val matched = storage.get(Url("https://example.com/api/users/profile"))
        assertEquals(1, matched.size)
        assertEquals("token", matched.first().name)
        assertEquals("abc123xyz", matched.first().value)

        val unMatchedPath = storage.get(Url("https://example.com/other/path"))
        assertTrue(unMatchedPath.isEmpty())

        val unMatchedDomain = storage.get(Url("https://another.com/api/users"))
        assertTrue(unMatchedDomain.isEmpty())
    }

    @Test
    fun testCookieDomainSubdomainMatching() = runBlocking {
        val cookie = Cookie(
            name = "shared_auth",
            value = "shared_value",
            domain = "example.com",
            path = "/",
        )

        storage.addCookie(Url("https://example.com/"), cookie)

        val subDomainMatch = storage.get(Url("https://sub.example.com/dashboard"))
        assertEquals(1, subDomainMatch.size)
        assertEquals("shared_auth", subDomainMatch.first().name)
    }

    @Test
    fun testPathSorting() = runBlocking {
        val rootCookie = Cookie(name = "c_root", value = "1", domain = "example.com", path = "/")
        val apiCookie = Cookie(name = "c_api", value = "2", domain = "example.com", path = "/api")
        val v1Cookie = Cookie(name = "c_v1", value = "3", domain = "example.com", path = "/api/v1")

        storage.addCookie(Url("https://example.com/"), rootCookie)
        storage.addCookie(Url("https://example.com/api"), apiCookie)
        storage.addCookie(Url("https://example.com/api/v1"), v1Cookie)

        val result = storage.get(Url("https://example.com/api/v1/details"))
        assertEquals(3, result.size)
        // 应该按 path 长度降序排序: /api/v1 -> /api -> /
        assertEquals("c_v1", result[0].name)
        assertEquals("c_api", result[1].name)
        assertEquals("c_root", result[2].name)
    }

    @Test
    fun testOverwriteSameCookie() = runBlocking {
        val cookie1 = Cookie(name = "session_id", value = "v1", domain = "example.com", path = "/app")
        val cookie2 = Cookie(name = "session_id", value = "v2", domain = "example.com", path = "/app")

        storage.addCookie(Url("https://example.com/app"), cookie1)
        storage.addCookie(Url("https://example.com/app"), cookie2)

        val all = storage.getAll()
        assertEquals(1, all.size)
        assertEquals("v2", all.first().value)
    }

    @Test
    fun testExpirationAndMaxAge() = runBlocking {
        val now = GMTDate().timestamp

        // 已过期 Cookie（expires 在过去）
        val expiredCookie = Cookie(
            name = "expired",
            value = "old",
            domain = "example.com",
            path = "/",
            expires = GMTDate(now - 100_000),
        )
        // 有效 Cookie（带有未来 expires）
        val validCookie = Cookie(
            name = "valid",
            value = "new",
            domain = "example.com",
            path = "/",
            expires = GMTDate(now + 100_000),
        )
        // 会话 Cookie（无 expires，默认 maxAge=0）
        val sessionCookie = Cookie(
            name = "session_cookie",
            value = "session_val",
            domain = "example.com",
            path = "/",
        )
        // maxAge = -1 表示已失效/删除指令
        val deleteCookie = Cookie(
            name = "valid",
            value = "deleted",
            domain = "example.com",
            path = "/",
            maxAge = -1,
        )

        storage.addCookie(Url("https://example.com/"), expiredCookie)
        storage.addCookie(Url("https://example.com/"), validCookie)
        storage.addCookie(Url("https://example.com/"), sessionCookie)

        val cookies = storage.get(Url("https://example.com/"))
        assertEquals(2, cookies.size)
        assertTrue(cookies.any { it.name == "valid" && it.value == "new" })
        assertTrue(cookies.any { it.name == "session_cookie" && it.value == "session_val" })

        // 发送 maxAge = -1 删除指令
        storage.addCookie(Url("https://example.com/"), deleteCookie)
        val afterDelete = storage.get(Url("https://example.com/"))
        assertEquals(1, afterDelete.size)
        assertEquals("session_cookie", afterDelete.first().name)
    }

    @Test
    fun testBatchAndManagementMethods() = runBlocking {
        val list = listOf(
            Cookie(name = "c1", value = "v1", domain = "site1.com", path = "/"),
            Cookie(name = "c2", value = "v2", domain = "site2.com", path = "/"),
            Cookie(name = "c3", value = "v3", domain = "site1.com", path = "/sub"),
        )
        storage.setCookies(list)

        assertEquals(3, storage.getAll().size)

        // 删除特定域名的 Cookie
        storage.remove { it.domain == "site1.com" }
        val remaining = storage.getAll()
        assertEquals(1, remaining.size)
        assertEquals("site2.com", remaining.first().domain)

        // 全部清空
        storage.clear()
        assertTrue(storage.getAll().isEmpty())
    }

    @Test
    fun testPersistenceAcrossInstances() = runBlocking {
        val url = Url("https://persist.example.com/")
        storage.addCookie(url, Cookie(name = "persisted", value = "saved_data", domain = "persist.example.com", path = "/"))

        // 新建 Storage 指向同一个数据库
        val secondStorage = RoomActionCookiesStorage(database)
        val retrieved = secondStorage.get(url)

        assertEquals(1, retrieved.size)
        assertEquals("persisted", retrieved.first().name)
        assertEquals("saved_data", retrieved.first().value)
    }
}
