package com.xiaoyv.workflow.editor.bridge.contract

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EditorBridgeContractTest {
    @Test
    fun saveRequestRoundTripsWithWorkflowRevision() {
        val request =
            SaveWorkflowRequest(
                baseRevision = 7,
                workflow = ActionWorkflow(id = "sample", name = "Sample"),
            )

        val decoded =
            defaultJson.decodeFromString(
                SaveWorkflowRequest.serializer(),
                defaultJson.encodeToString(SaveWorkflowRequest.serializer(), request),
            )

        assertEquals(request, decoded)
    }

    @Test
    fun eventRoundTripsWithoutExecutionPayload() {
        val event =
            EditorBridgeEvent(
                sequence = 2,
                type = EditorBridgeEventType.NODE_COMPLETED,
                timestampMillis = 100,
                runId = "run-1",
                nodeId = "delay",
                message = "completed",
            )

        val decoded =
            defaultJson.decodeFromString(
                EditorBridgeEvent.serializer(),
                defaultJson.encodeToString(EditorBridgeEvent.serializer(), event),
            )

        assertEquals(event, decoded)
    }

    @Test
    fun repositoryUsesOptimisticRevision() = runTest {
        val repository = InMemoryEditorWorkflowRepository()
        val workflow = ActionWorkflow(id = "sample", name = "Sample")

        val saved = repository.save("sample", 0, workflow) as EditorSaveResult.Saved
        val conflict = repository.save("sample", 0, workflow) as EditorSaveResult.Conflict

        assertEquals(1, saved.document.revision)
        assertEquals(saved.document, conflict.current)
    }

    @Test
    fun serviceMigratesValidatesAndOnlyPersistsValidWorkflow() = runTest {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.node",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "Test node"),
                            ),
                        executor = { _, _ -> error("not run") },
                    )
                )
            )
        val service =
            EditorWorkflowService(
                registry,
                ActionWorkflowValidator(registry),
                InMemoryEditorWorkflowRepository(),
                ActionEditorManifestCodec(defaultJson, registry),
            )
        val valid =
            ActionWorkflow(
                id = "valid",
                name = "Valid",
                entryNodeId = "node",
                nodes = listOf(ActionNode("node", "test.node")).toPersistentList(),
            )
        val invalid = valid.copy(entryNodeId = "missing")

        assertTrue(service.validate(ValidateWorkflowRequest(valid)).validation.isValid)
        assertIs<EditorSaveResult.Saved>(service.save("valid", SaveWorkflowRequest(0, valid)))
        val rejected = service.save("invalid", SaveWorkflowRequest(0, invalid))
        assertIs<EditorSaveResult.Invalid>(rejected)
        assertFalse(rejected.response.validation.isValid)
        assertEquals(null, service.getWorkflow("invalid"))
    }
}
