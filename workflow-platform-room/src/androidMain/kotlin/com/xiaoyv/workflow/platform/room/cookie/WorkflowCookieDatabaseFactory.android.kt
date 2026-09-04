package com.xiaoyv.workflow.platform.room.cookie

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

@SuppressLint("PrivateApi")
actual fun createPlatformDatabaseBuilder(dbPath: String): RoomDatabase.Builder<WorkflowCookieDatabase> {
    val context: Context = getPlatformContext()
    return Room.databaseBuilder<WorkflowCookieDatabase>(
        context = context,
        name = dbPath,
    ) {
        WorkflowCookieDatabaseConstructor.initialize()
    }
}

@SuppressLint("PrivateApi")
actual fun createPlatformInMemoryDatabaseBuilder(): RoomDatabase.Builder<WorkflowCookieDatabase> {
    val context: Context = getPlatformContext()
    return Room.inMemoryDatabaseBuilder<WorkflowCookieDatabase>(
        context = context,
    ) {
        WorkflowCookieDatabaseConstructor.initialize()
    }
}

@SuppressLint("PrivateApi")
private fun getPlatformContext(): Context {
    return runCatching { FileKit.context }.getOrElse {
        runCatching {
            Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as? Context
        }.getOrNull() ?: runCatching {
            Class.forName("androidx.test.core.app.ApplicationProvider")
                .getMethod("getApplicationContext")
                .invoke(null) as? Context
        }.getOrNull() ?: createMockContext()
    }
}

private class MockPlatformContext : android.content.ContextWrapper(null) {
    private val tempDir = java.io.File(System.getProperty("java.io.tmpdir", ".")).apply { mkdirs() }

    override fun getApplicationContext(): Context = this

    override fun getDatabasePath(name: String): java.io.File {
        val file = java.io.File(name)
        return if (file.isAbsolute) file else java.io.File(tempDir, name)
    }

    override fun getPackageName(): String = "com.xiaoyv.workflow.platform.room"

    override fun getCacheDir(): java.io.File = tempDir

    override fun getDataDir(): java.io.File = tempDir

    override fun getFilesDir(): java.io.File = tempDir

    override fun getNoBackupFilesDir(): java.io.File = tempDir

    override fun getSystemService(name: String): Any? = null

    override fun getSystemServiceName(serviceClass: Class<*>): String? = null
}

private fun createMockContext(): Context = MockPlatformContext()
