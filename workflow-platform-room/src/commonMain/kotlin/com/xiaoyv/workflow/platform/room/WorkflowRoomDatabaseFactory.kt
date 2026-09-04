package com.xiaoyv.workflow.platform.room

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.FileSystem
import okio.SYSTEM

expect fun createPlatformDatabaseBuilder(dbPath: String): RoomDatabase.Builder<WorkflowRoomDatabase>
expect fun createPlatformInMemoryDatabaseBuilder(): RoomDatabase.Builder<WorkflowRoomDatabase>

object WorkflowRoomDatabaseFactory {

    fun createDatabase(
        dbPath: String = defaultDbPath(),
    ): WorkflowRoomDatabase {
        return createPlatformDatabaseBuilder(dbPath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    fun createInMemoryDatabase(): WorkflowRoomDatabase {
        return createPlatformInMemoryDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    private fun defaultDbPath(): String {
        return runCatching {
            val dir = (FileKit.filesDir / "workflow").apply { createDirectories() }
            val file = dir / "workflow.db"
            file.path
        }.getOrElse {
            val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "workflow"
            FileSystem.SYSTEM.createDirectories(dir)
            (dir / "workflow.db").toString()
        }
    }
}
