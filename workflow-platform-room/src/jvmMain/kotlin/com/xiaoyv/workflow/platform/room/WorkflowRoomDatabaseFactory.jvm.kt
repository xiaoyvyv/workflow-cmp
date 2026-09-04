package com.xiaoyv.workflow.platform.room

import androidx.room3.Room
import androidx.room3.RoomDatabase

actual fun createPlatformDatabaseBuilder(dbPath: String): RoomDatabase.Builder<WorkflowRoomDatabase> {
    return Room.databaseBuilder<WorkflowRoomDatabase>(
        name = dbPath,
    )
}

actual fun createPlatformInMemoryDatabaseBuilder(): RoomDatabase.Builder<WorkflowRoomDatabase> {
    return Room.inMemoryDatabaseBuilder<WorkflowRoomDatabase>()
}
