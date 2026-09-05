package com.xiaoyv.workflow.editor.bridge

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeEvent
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeEventType
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeRunStatusValue
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorEngineRunControllerTest {
    @Test
    fun startsSavedRevisionPublishesEventsAndRejectsWrongRevision() = runBlocking {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.done",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "完成节点"),
                            ),
                        executor = { _, _ -> ActionNodeExecutionResult(outputPortId = "") },
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
        val workflow =
            ActionWorkflow(
                id = "demo",
                name = "Demo",
                entryNodeId = "entry",
                nodes = persistentListOf(ActionNode("entry", "test.done")),
            )
        service.save("demo", SaveWorkflowRequest(0, workflow))
        val completed = CompletableDeferred<EditorBridgeEvent>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller =
            EditorEngineRunController(
                service,
                ActionWorkflowEngine(registry, ActionWorkflowValidator(registry), now = { 10L }),
                ActionSideEffectHandler { error("no effect") },
                scope,
                { if (it.type == EditorBridgeEventType.RUN_COMPLETED) completed.complete(it) },
                now = { 10L },
            )

        assertNull(controller.start("demo", revision = 999))
        val descriptor = controller.start("demo", revision = 1)
        assertNotNull(descriptor)
        assertEquals(EditorBridgeRunStatusValue.RUNNING, descriptor.status)
        assertEquals(descriptor.runId, withTimeout(2_000) { completed.await() }.runId)
        val status = assertNotNull(controller.status(descriptor.runId))
        assertEquals(EditorBridgeRunStatusValue.COMPLETED, status.run.status)
        assertTrue(status.events.any { it.type == EditorBridgeEventType.RUN_COMPLETED })
        assertTrue(!controller.cancel(descriptor.runId))
        scope.cancel()
    }

    @Test
    fun cancellationIsRetainedAsTerminalRunStatus() = runBlocking {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.blocking",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "阻塞节点"),
                            ),
                        executor = { _, _ -> awaitCancellation() },
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
                0,
                ActionWorkflow(
                    id = "demo",
                    name = "Demo",
                    entryNodeId = "entry",
                    nodes = persistentListOf(ActionNode("entry", "test.blocking")),
                ),
            ),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller =
            EditorEngineRunController(
                service,
                ActionWorkflowEngine(registry, validator, now = { 10L }),
                ActionSideEffectHandler { error("no effect") },
                scope,
            )

        val descriptor = assertNotNull(controller.start("demo"))
        assertTrue(controller.cancel(descriptor.runId))
        assertEquals(
            EditorBridgeRunStatusValue.CANCELLED,
            assertNotNull(controller.status(descriptor.runId)).run.status,
        )
        scope.cancel()
    }

    @Test
    fun sideEffectRequestedPublishesFormattedDescription() = runBlocking {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.toast",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "提示节点"),
                            ),
                        executor = { _, _ ->
                            ActionNodeExecutionResult(
                                outputPortId = "",
                                sideEffect = com.xiaoyv.workflow.node.effect.ActionShowToastEffect("测试提示"),
                            )
                        },
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
                0,
                ActionWorkflow(
                    id = "demo",
                    name = "Demo",
                    entryNodeId = "entry",
                    nodes = persistentListOf(ActionNode("entry", "test.toast")),
                ),
            ),
        )
        val sideEffectEvent = CompletableDeferred<EditorBridgeEvent>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller =
            EditorEngineRunController(
                service,
                ActionWorkflowEngine(registry, validator, now = { 10L }),
                ActionSideEffectHandler { com.xiaoyv.workflow.engine.ActionSideEffectResult.Success() },
                scope,
                { if (it.type == EditorBridgeEventType.SIDE_EFFECT_REQUESTED) sideEffectEvent.complete(it) },
                now = { 10L },
            )

        val descriptor = assertNotNull(controller.start("demo"))
        val event = withTimeout(2_000) { sideEffectEvent.await() }
        assertEquals(descriptor.runId, event.runId)
        assertEquals("entry", event.nodeId)
        assertEquals("弹窗提示 \"测试提示\"", event.message)
        scope.cancel()
    }

    @Test
    fun failedExecutionPublishesFormattedTraceLogAndNodeId() = runBlocking {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.fail",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "失败节点"),
                            ),
                        executor = { _, _ ->
                            throw com.xiaoyv.workflow.exception.ActionNodeExecutionException(
                                code = "TEST_ERROR",
                                messageText = "模拟执行失败",
                                nodeId = "entry",
                                hint = "请检查测试用例",
                            )
                        },
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
                0,
                ActionWorkflow(
                    id = "demo",
                    name = "Demo",
                    entryNodeId = "entry",
                    nodes = persistentListOf(ActionNode("entry", "test.fail")),
                ),
            ),
        )
        val failedEvent = CompletableDeferred<EditorBridgeEvent>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller =
            EditorEngineRunController(
                service,
                ActionWorkflowEngine(registry, validator, now = { 10L }),
                ActionSideEffectHandler { com.xiaoyv.workflow.engine.ActionSideEffectResult.Success() },
                scope,
                { if (it.type == EditorBridgeEventType.RUN_FAILED) failedEvent.complete(it) },
                now = { 10L },
            )

        val descriptor = assertNotNull(controller.start("demo"))
        val event = withTimeout(2_000) { failedEvent.await() }
        assertEquals(descriptor.runId, event.runId)
        assertEquals("entry", event.nodeId)
        val msg = assertNotNull(event.message)
        assertTrue(msg.contains("[WORKFLOW ERROR TRACE]"))
        assertTrue(msg.contains("TEST_ERROR"))
        assertTrue(msg.contains("模拟执行失败"))
        assertTrue(msg.contains("请检查测试用例"))
        scope.cancel()
    }
}
