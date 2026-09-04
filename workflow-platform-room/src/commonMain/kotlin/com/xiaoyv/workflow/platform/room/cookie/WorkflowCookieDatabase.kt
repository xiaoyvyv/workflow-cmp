package com.xiaoyv.workflow.platform.room.cookie

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Cookie Room 数据库定义。
 */
@Database(
    entities = [RoomActionCookieEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(WorkflowCookieDatabaseConstructor::class)
abstract class WorkflowCookieDatabase : RoomDatabase() {
    abstract fun cookieDao(): RoomActionCookieDao
}

// Room 编译器通过 KSP 为各个平台自动生成此构造器的实际实现
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object WorkflowCookieDatabaseConstructor : RoomDatabaseConstructor<WorkflowCookieDatabase> {
    override fun initialize(): WorkflowCookieDatabase
}
