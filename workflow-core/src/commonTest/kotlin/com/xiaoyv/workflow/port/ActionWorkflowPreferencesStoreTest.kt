package com.xiaoyv.workflow.port

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xiaoyv.workflow.port.impl.DefaultActionWorkflowPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okio.FileSystem
import okio.SYSTEM
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActionWorkflowPreferencesStoreTest {

    private val testPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test_workflow_pref_${Random.nextLong().toString(16)}.preferences_pb"
    private val dataStore = PreferenceDataStoreFactory.createWithPath {
        testPath
    }
    private val store = DefaultActionWorkflowPreferencesStore(dataStore)

    @AfterTest
    fun tearDown() {
        runCatching { FileSystem.SYSTEM.delete(testPath) }
    }

    @Test
    fun testSetAndGetPrimitives() = runBlocking {
        store.set("str_key", JsonPrimitive("hello world"))
        store.set("int_key", JsonPrimitive(42))
        store.set("bool_key", JsonPrimitive(true))

        assertEquals(JsonPrimitive("hello world"), store.get("str_key"))
        assertEquals(JsonPrimitive(42), store.get("int_key"))
        assertEquals(JsonPrimitive(true), store.get("bool_key"))
    }

    @Test
    fun testSetAndGetComplexJson() = runBlocking {
        val obj = JsonObject(mapOf("name" to JsonPrimitive("Bocchi"), "id" to JsonPrimitive(1)))
        val arr = JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b")))

        store.set("obj_key", obj)
        store.set("arr_key", arr)

        assertEquals(obj, store.get("obj_key"))
        assertEquals(arr, store.get("arr_key"))
    }

    @Test
    fun testJsonNullHandling() = runBlocking {
        assertFalse(store.has("null_key"))
        assertNull(store.get("null_key"))

        store.set("null_key", JsonNull)

        assertTrue(store.has("null_key"))
        assertEquals(JsonNull, store.get("null_key"))
    }

    @Test
    fun testDelete() = runBlocking {
        store.set("to_delete", JsonPrimitive("value"))
        assertTrue(store.has("to_delete"))

        store.delete("to_delete")

        assertFalse(store.has("to_delete"))
        assertNull(store.get("to_delete"))
    }

    @Test
    fun testClearOnlyWorkflowKeys() = runBlocking {
        // 在同一个 DataStore 中存入非 workflow 键
        val otherKey = stringPreferencesKey("app_setting_key")
        dataStore.edit { it[otherKey] = "app_value" }

        store.set("wf_key_1", JsonPrimitive("val1"))
        store.set("wf_key_2", JsonPrimitive("val2"))

        assertTrue(store.has("wf_key_1"))
        assertTrue(store.has("wf_key_2"))

        store.clear()

        assertFalse(store.has("wf_key_1"))
        assertFalse(store.has("wf_key_2"))

        // 验证非 workflow 键不受影响
        val prefs = dataStore.data.first()
        assertEquals("app_value", prefs[otherKey])
    }
}
