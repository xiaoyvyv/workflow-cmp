package com.xiaoyv.workflow.editor.mcp

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkflowMcpServiceTest {
    private val registry =
        ActionNodeRegistry(
            listOf(
                ActionNodeDefinition(
                    spec =
                        ActionNodeSpec(
                            "demo.node",
                            category = "demo",
                            editor = ActionNodeEditorSpec("Demo"),
                        ),
                    executor = { _, _ -> error("not executed") },
                )
            )
        )
    private val service =
        WorkflowMcpService(
            EditorWorkflowService(
                registry,
                ActionWorkflowValidator(registry),
                InMemoryEditorWorkflowRepository(),
                ActionEditorManifestCodec(defaultJson, registry),
            ),
            defaultJson,
            "architecture",
        )

    @Test
    fun readsArchitectureAndNodeResources() {
        assertTrue(
            service.readResource(WorkflowMcpService.ARCHITECTURE_URI).contains("architecture")
        )
        assertTrue(service.readResource("workflow://nodes/demo.node").contains("demo.node"))
        assertTrue(service.readResource("workflow://categories/demo").contains("demo.node"))
    }

    @Test
    fun validatesCandidateWorkflowThroughCore() {
        val response =
            service.validateWorkflow("""{"id":"test","name":"test","entryNodeId":"missing"}""")
        assertTrue(!response.validation.isValid)
    }

    @Test
    fun savesOnlyThroughSharedValidationService() {
        runBlocking {
            val saved =
                service.saveWorkflow(
                    "demo",
                    0,
                    """{"id":"demo","name":"Demo","entryNodeId":"entry","nodes":[{"id":"entry","type":"demo.node"}]}""",
                )
            assertIs<EditorSaveResult.Saved>(saved)
            assertIs<EditorSaveResult.Conflict>(
                service.saveWorkflow(
                    "demo",
                    0,
                    """{"id":"demo","name":"Demo","entryNodeId":"entry","nodes":[{"id":"entry","type":"demo.node"}]}""",
                )
            )
        }
    }
}
