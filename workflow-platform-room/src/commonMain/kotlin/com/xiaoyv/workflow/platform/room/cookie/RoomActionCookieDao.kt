package com.xiaoyv.workflow.platform.room.cookie

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

/**
 * Cookie 数据访问接口（Room DAO）。
 */
@Dao
interface RoomActionCookieDao {

    @Query("SELECT * FROM workflow_cookies")
    suspend fun getAllCookies(): List<RoomActionCookieEntity>

    @Query("SELECT * FROM workflow_cookies WHERE domain = :domain")
    suspend fun getCookiesByDomain(domain: String): List<RoomActionCookieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cookie: RoomActionCookieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(cookies: List<RoomActionCookieEntity>)

    @Query("DELETE FROM workflow_cookies WHERE name = :name AND (domain = :domain OR (domain IS NULL AND :domain IS NULL)) AND (path = :path OR (path IS NULL AND :path IS NULL))")
    suspend fun deleteCookie(name: String, domain: String?, path: String?)

    @Query("DELETE FROM workflow_cookies WHERE domain = :domain")
    suspend fun deleteCookiesByDomain(domain: String)

    @Query("DELETE FROM workflow_cookies WHERE expiresTimestamp IS NOT NULL AND expiresTimestamp < :now")
    suspend fun deleteExpiredCookies(now: Long)

    @Query("DELETE FROM workflow_cookies WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM workflow_cookies")
    suspend fun clearAll()
}
