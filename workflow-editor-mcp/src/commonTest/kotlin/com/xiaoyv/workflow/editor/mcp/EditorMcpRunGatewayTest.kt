package com.xiaoyv.workflow.editor.mcp

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.editor.bridge.EditorEngineRunController
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.editor.bridge.contract.SaveWorkflowRequest
import com.xiaoyv.workflow.engine.ActionSideEffectHandler
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine
import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.execution.ActionNodeExecutionResult
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorMcpRunGatewayTest {
    @Test
    fun delegatesRunLifecycleToTheSharedDeviceController() = runBlocking {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.done",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "测试完成节点"),
                            ),
                        executor = { _, _ -> ActionNodeExecutionResult(outputPortId = "") },
                    )
                )
            )
        val validator = ActionWorkflowValidator(registry)
        val service =
            EditorWorkflowService(
                registry,
                validator,
                InMemoryEditorWorkflowRepository(),
                ActionEditorManifestCodec(defaultJson, registry),
            )
        service.save(
            "demo",
            SaveWorkflowRequest(
                baseRevision = 0,
                workflow =
                    ActionWorkflow(
                        id = "demo",
                        name = "Demo",
                        entryNodeId = "entry",
                        nodes = persistentListOf(ActionNode("entry", "test.done")),
                    ),
            ),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val gateway =
            EditorMcpRunGateway(
                EditorEngineRunController(
                    service,
                    ActionWorkflowEngine(registry, validator, now = { 10L }),
                    ActionSideEffectHandler { error("No side effect is expected") },
                    scope,
                )
            )

        val started = gateway.start("demo", revision = 1)

        assertTrue(started is WorkflowMcpRunResult.Started)
        assertEquals("demo", started.workflowId)
        assertFalse(gateway.cancel("unknown"))
        assertNull(gateway.status("unknown"))
        scope.cancel()
    }
}
