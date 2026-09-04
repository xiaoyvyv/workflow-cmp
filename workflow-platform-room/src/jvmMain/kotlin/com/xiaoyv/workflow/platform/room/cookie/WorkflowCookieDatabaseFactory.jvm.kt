package com.xiaoyv.workflow.platform.room.cookie

import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createPlatformDatabaseBuilder(dbPath: String): RoomDatabase.Builder<WorkflowCookieDatabase> {
    return Room.databaseBuilder<WorkflowCookieDatabase>(
        name = dbPath,
        factory = { WorkflowCookieDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver())
}

actual fun createPlatformInMemoryDatabaseBuilder(): RoomDatabase.Builder<WorkflowCookieDatabase> {
    return Room.inMemoryDatabaseBuilder<WorkflowCookieDatabase>(
        factory = { WorkflowCookieDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver())
}
