package com.xiaoyv.workflow.platform.room.cookie

import androidx.room.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.FileSystem
import okio.SYSTEM

expect fun createPlatformDatabaseBuilder(dbPath: String): RoomDatabase.Builder<WorkflowCookieDatabase>
expect fun createPlatformInMemoryDatabaseBuilder(): RoomDatabase.Builder<WorkflowCookieDatabase>

object WorkflowCookieDatabaseFactory {

    fun createDatabase(
        dbPath: String = defaultDbPath(),
    ): WorkflowCookieDatabase {
        return createPlatformDatabaseBuilder(dbPath)
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    fun createInMemoryDatabase(): WorkflowCookieDatabase {
        return createPlatformInMemoryDatabaseBuilder()
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    private fun defaultDbPath(): String {
        return runCatching {
            val dir = (FileKit.filesDir / "workflow").apply { createDirectories() }
            val file = dir / "workflow_cookies.db"
            file.path
        }.getOrElse {
            val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "workflow"
            FileSystem.SYSTEM.createDirectories(dir)
            (dir / "workflow_cookies.db").toString()
        }
    }
}
