package com.xiaoyv.workflow.port.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore
import com.xiaoyv.workflow.util.defaultJson
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonElement
import okio.Path.Companion.toPath

private val workflowDataStore by lazy {
    val path = runCatching {
        val dir = (FileKit.filesDir / "workflow").apply { createDirectories() }
        val file = dir / "preferences.preferences_pb"
        file.path.toPath(true)
    }.getOrElse {
        okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "workflow" / "preferences.preferences_pb"
    }
    PreferenceDataStoreFactory.createWithPath { path }
}

/**
 * 基于 [DataStore] 的工作流持久化存储实现。
 */
class DefaultActionWorkflowPreferencesStore(
    val dataStore: DataStore<Preferences> = workflowDataStore,
) : ActionWorkflowPreferencesStore {

    override suspend fun get(key: String): JsonElement? {
        val prefKey = stringPreferencesKey(ActionWorkflowPreferencesStore.storageKey(key))
        val prefs = dataStore.data.first()
        val raw = prefs[prefKey] ?: return null
        return defaultJson.parseToJsonElement(raw)
    }

    override suspend fun set(key: String, value: JsonElement) {
        val prefKey = stringPreferencesKey(ActionWorkflowPreferencesStore.storageKey(key))
        val raw = defaultJson.encodeToString(JsonElement.serializer(), value)
        dataStore.edit { preferences ->
            preferences[prefKey] = raw
        }
    }

    override suspend fun delete(key: String) {
        val prefKey = stringPreferencesKey(ActionWorkflowPreferencesStore.storageKey(key))
        dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }

    override suspend fun has(key: String): Boolean {
        val prefKey = stringPreferencesKey(ActionWorkflowPreferencesStore.storageKey(key))
        val prefs = dataStore.data.first()
        return prefs.contains(prefKey)
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter {
                it.name.startsWith(ActionWorkflowPreferencesStore.KEY_PREFIX)
            }
            keysToRemove.forEach { key ->
                preferences.remove(key)
            }
        }
    }
}
