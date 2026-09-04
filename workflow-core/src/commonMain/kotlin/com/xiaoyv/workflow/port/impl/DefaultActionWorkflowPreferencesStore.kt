package com.xiaoyv.workflow.port.impl

import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

/**
 * 默认内存存储实现。宿主可提供持久化实现替换它。
 */
class DefaultActionWorkflowPreferencesStore : ActionWorkflowPreferencesStore {
    private val lock = Mutex()
    private val values = mutableMapOf<String, JsonElement>()

    override suspend fun get(key: String): JsonElement? = lock.withLock {
        values[ActionWorkflowPreferencesStore.storageKey(key)]
    }

    override suspend fun set(key: String, value: JsonElement) = lock.withLock {
        values[ActionWorkflowPreferencesStore.storageKey(key)] = value
    }

    override suspend fun delete(key: String) {
        lock.withLock { values.remove(ActionWorkflowPreferencesStore.storageKey(key)) }
    }

    override suspend fun has(key: String): Boolean = lock.withLock {
        ActionWorkflowPreferencesStore.storageKey(key) in values
    }

    override suspend fun clear() {
        lock.withLock { values.keys.removeAll { it.startsWith(ActionWorkflowPreferencesStore.KEY_PREFIX) } }
    }
}
