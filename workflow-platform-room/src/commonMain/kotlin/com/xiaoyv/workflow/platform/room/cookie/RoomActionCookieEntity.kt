package com.xiaoyv.workflow.platform.room.cookie

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.util.date.GMTDate

/**
 * Cookie 数据库持久化实体。
 */
@Entity(
    tableName = "workflow_cookies",
    indices = [
        Index(value = ["domain", "name", "path"], unique = true),
        Index(value = ["domain"]),
        Index(value = ["expiresTimestamp"]),
    ],
)
data class RoomActionCookieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val value: String,
    val encoding: String = CookieEncoding.URI_ENCODING.name,
    val maxAge: Int? = null,
    val expiresTimestamp: Long? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val createdAt: Long = 0L,
) {
    fun isExpired(now: Long = GMTDate().timestamp): Boolean {
        // 1. maxAge > 0 按照创建时间计算绝对过期时刻
        if (maxAge != null && maxAge > 0) {
            val maxAgeExpires = createdAt + maxAge * 1000L
            if (now > maxAgeExpires) return true
        }
        // 2. maxAge < 0 属于直接失效
        if (maxAge != null && maxAge < 0) {
            return true
        }
        // 3. expires 绝对时间戳过期判定
        if (expiresTimestamp != null && expiresTimestamp < now) {
            return true
        }
        return false
    }

    fun toCookie(): Cookie {
        val enc = runCatching { CookieEncoding.valueOf(encoding) }.getOrDefault(CookieEncoding.URI_ENCODING)
        return Cookie(
            name = name,
            value = value,
            encoding = enc,
            maxAge = maxAge ?: 0,
            expires = expiresTimestamp?.let { GMTDate(it) },
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
        )
    }

    companion object {
        fun fromCookie(cookie: Cookie, createdAt: Long = GMTDate().timestamp): RoomActionCookieEntity {
            return RoomActionCookieEntity(
                name = cookie.name,
                value = cookie.value,
                encoding = cookie.encoding.name,
                maxAge = cookie.maxAge,
                expiresTimestamp = cookie.expires?.timestamp,
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                createdAt = createdAt,
            )
        }
    }
}
