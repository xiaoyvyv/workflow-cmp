package com.xiaoyv.workflow.platform.room

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.xiaoyv.workflow.platform.room.cookie.RoomActionCookieDao
import com.xiaoyv.workflow.platform.room.cookie.RoomActionCookieEntity

/**
 * 工作流平台 Room 数据库定义。
 */
@Database(
    entities = [RoomActionCookieEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(WorkflowRoomDatabaseConstructor::class)
abstract class WorkflowRoomDatabase : RoomDatabase() {
    abstract fun cookieDao(): RoomActionCookieDao
}

// Room 编译器通过 KSP 为各个平台自动生成此构造器的实际实现
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object WorkflowRoomDatabaseConstructor : RoomDatabaseConstructor<WorkflowRoomDatabase> {
    override fun initialize(): WorkflowRoomDatabase
}
