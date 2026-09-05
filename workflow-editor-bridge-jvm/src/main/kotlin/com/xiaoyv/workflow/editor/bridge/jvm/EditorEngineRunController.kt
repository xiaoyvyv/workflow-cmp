package com.xiaoyv.workflow.editor.bridge.jvm

import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeEvent
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeEventType
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeProtocol
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeRunStatusValue
import com.xiaoyv.workflow.editor.bridge.contract.EditorRunDescriptor
import com.xiaoyv.workflow.editor.bridge.contract.EditorRunStatus
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.engine.ActionSideEffectHandler
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine
import com.xiaoyv.workflow.model.execution.ActionExecutionContext
import com.xiaoyv.workflow.model.execution.ActionExecutionEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 在设备端运行不可变的已保存版本；浏览器客户端只能观察事件。
 */
class EditorEngineRunController(
    private val service: EditorWorkflowService,
    private val engine: ActionWorkflowEngine,
    private val sideEffectHandler: ActionSideEffectHandler,
    private val scope: CoroutineScope,
    private var publish: (EditorBridgeEvent) -> Unit = {},
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    private val jobs = ConcurrentHashMap<String, Job>()
    private val runs = ConcurrentHashMap<String, RunState>()

    /**
     * 由传输层在构造后调用，使引擎与 Ktor 保持解耦。
     */
    fun setEventPublisher(publisher: (EditorBridgeEvent) -> Unit) {
        publish = publisher
    }

    suspend fun start(workflowId: String, revision: Long? = null): EditorRunDescriptor? {
        val document = service.getWorkflow(workflowId) ?: return null
        if (revision != null && revision != document.revision) return null
        val runId = UUID.randomUUID().toString()
        val descriptor =
            EditorRunDescriptor(
                runId,
                workflowId,
                document.revision,
                EditorBridgeRunStatusValue.RUNNING,
            )
        val state = RunState(descriptor)
        runs[runId] = state
        jobs[runId] = scope.launch {
            try {
                engine
                    .execute(document.workflow, ActionExecutionContext(), sideEffectHandler)
                    .collect { event ->
                        val bridgeEvent = event.toBridgeEvent(runId, now())
                        state.record(bridgeEvent)
                        publish(bridgeEvent)
                    }
                state.completeIfRunning()
            } catch (_: CancellationException) {
                state.cancel()
            } catch (error: Exception) {
                val failed =
                    EditorBridgeEvent(
                        EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                        EditorBridgeEventType.RUN_FAILED,
                        now(),
                        runId,
                        message = error.message.orEmpty(),
                    )
                state.record(failed)
                publish(failed)
            } finally {
                jobs.remove(runId)
            }
        }
        return descriptor
    }

    fun cancel(runId: String): Boolean =
        jobs.remove(runId)?.let {
            runs[runId]?.cancel()
            it.cancel()
            true
        } ?: false

    fun status(runId: String): EditorRunStatus? = runs[runId]?.snapshot()

    private class RunState(private val descriptor: EditorRunDescriptor) {
        private val events = ArrayDeque<EditorBridgeEvent>()
        private var status = descriptor.status

        @Synchronized
        fun record(event: EditorBridgeEvent) {
            if (events.size == MAX_RETAINED_EVENTS) events.removeFirst()
            events.addLast(event)
            status =
                when (event.type) {
                    EditorBridgeEventType.RUN_COMPLETED -> EditorBridgeRunStatusValue.COMPLETED
                    EditorBridgeEventType.RUN_FAILED -> EditorBridgeRunStatusValue.FAILED
                    else -> status
                }
        }

        @Synchronized
        fun cancel() {
            status = EditorBridgeRunStatusValue.CANCELLED
        }

        @Synchronized
        fun completeIfRunning() {
            if (status == EditorBridgeRunStatusValue.RUNNING) {
                status = EditorBridgeRunStatusValue.COMPLETED
            }
        }

        @Synchronized
        fun snapshot() = EditorRunStatus(descriptor.copy(status = status), events.toList())
    }

    private fun ActionExecutionEvent.toBridgeEvent(
        runId: String,
        timestamp: Long,
    ): EditorBridgeEvent =
        when (this) {
            is ActionExecutionEvent.Started ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.RUN_STARTED,
                    timestamp,
                    runId,
                    message = workflowId,
                )

            is ActionExecutionEvent.NodeStarted ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.NODE_STARTED,
                    timestamp,
                    runId,
                    nodeId,
                )

            is ActionExecutionEvent.NodeCompleted ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.NODE_COMPLETED,
                    timestamp,
                    runId,
                    nodeId,
                    outputPortId,
                )

            is ActionExecutionEvent.SideEffectRequested ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.SIDE_EFFECT_REQUESTED,
                    timestamp,
                    runId,
                    nodeId,
                )

            is ActionExecutionEvent.Completed ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.RUN_COMPLETED,
                    timestamp,
                    runId,
                )

            is ActionExecutionEvent.Failed ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.RUN_FAILED,
                    timestamp,
                    runId,
                    error.nodeId,
                    error.message,
                )
        }

    private companion object {
        const val MAX_RETAINED_EVENTS = 500
    }
}
