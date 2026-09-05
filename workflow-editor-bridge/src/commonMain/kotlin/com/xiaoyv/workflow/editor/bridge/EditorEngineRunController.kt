package com.xiaoyv.workflow.editor.bridge

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 在设备端运行不可变的已保存版本；浏览器客户端只能观察事件。全平台通用。
 */
class EditorEngineRunController(
    private val service: EditorWorkflowService,
    private val engine: ActionWorkflowEngine,
    private val sideEffectHandler: ActionSideEffectHandler,
    private val scope: CoroutineScope,
    private var publish: (EditorBridgeEvent) -> Unit = {},
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val mutex = Mutex()
    private val jobs = mutableMapOf<String, Job>()
    private val runs = mutableMapOf<String, RunState>()

    /**
     * 由传输层在构造后调用，使引擎与 Ktor 保持解耦。
     */
    fun setEventPublisher(publisher: (EditorBridgeEvent) -> Unit) {
        publish = publisher
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun start(workflowId: String, revision: Long? = null): EditorRunDescriptor? {
        val document = service.getWorkflow(workflowId) ?: return null
        if (revision != null && revision != document.revision) return null
        val runId = Uuid.random().toString()
        val descriptor =
            EditorRunDescriptor(
                runId,
                workflowId,
                document.revision,
                EditorBridgeRunStatusValue.RUNNING,
            )
        val state = RunState(descriptor)
        mutex.withLock {
            runs[runId] = state
            val job = scope.launch {
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
                    val formattedMsg = if (error is com.xiaoyv.workflow.exception.ActionWorkflowException) {
                        error.buildFormattedTraceLog()
                    } else {
                        buildString {
                            appendLine("================================================================================")
                            appendLine("❌ [WORKFLOW ERROR TRACE] 工作流执行异常溯源报告")
                            appendLine("--------------------------------------------------------------------------------")
                            appendLine("📍 错误原因   : ${error.message ?: error.toString()}")
                            appendLine("📍 底层异常   : ${error.stackTraceToString()}")
                            appendLine("================================================================================")
                        }
                    }
                    val failed =
                        EditorBridgeEvent(
                            EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                            EditorBridgeEventType.RUN_FAILED,
                            now(),
                            runId,
                            nodeId = (error as? com.xiaoyv.workflow.exception.ActionWorkflowException)?.nodeId,
                            message = formattedMsg,
                        )
                    state.record(failed)
                    publish(failed)
                } finally {
                    mutex.withLock {
                        jobs.remove(runId)
                    }
                }
            }
            jobs[runId] = job
        }
        return descriptor
    }

    suspend fun cancel(runId: String): Boolean = mutex.withLock {
        val state = runs[runId]
        if (state == null || state.snapshot().run.status != EditorBridgeRunStatusValue.RUNNING) {
            jobs.remove(runId)
            return@withLock false
        }
        val job = jobs.remove(runId)
        state.cancel()
        job?.cancel()
        true
    }

    suspend fun status(runId: String): EditorRunStatus? =
        mutex.withLock { runs[runId] }?.snapshot()

    private class RunState(private val descriptor: EditorRunDescriptor) {
        private val stateMutex = Mutex()
        private val events = ArrayDeque<EditorBridgeEvent>()
        private var status = descriptor.status

        suspend fun record(event: EditorBridgeEvent) = stateMutex.withLock {
            if (events.size == MAX_RETAINED_EVENTS) events.removeFirst()
            events.addLast(event)
            status =
                when (event.type) {
                    EditorBridgeEventType.RUN_COMPLETED -> EditorBridgeRunStatusValue.COMPLETED
                    EditorBridgeEventType.RUN_FAILED -> EditorBridgeRunStatusValue.FAILED
                    else -> status
                }
        }

        suspend fun cancel() = stateMutex.withLock {
            status = EditorBridgeRunStatusValue.CANCELLED
        }

        suspend fun completeIfRunning() = stateMutex.withLock {
            if (status == EditorBridgeRunStatusValue.RUNNING) {
                status = EditorBridgeRunStatusValue.COMPLETED
            }
        }

        suspend fun snapshot(): EditorRunStatus = stateMutex.withLock {
            EditorRunStatus(descriptor.copy(status = status), events.toList())
        }
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
                    message = effect.describe(),
                )

            is ActionExecutionEvent.Completed ->
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.RUN_COMPLETED,
                    timestamp,
                    runId,
                )

            is ActionExecutionEvent.Failed -> {
                EditorBridgeEvent(
                    EditorBridgeProtocol.UNASSIGNED_EVENT_SEQUENCE,
                    EditorBridgeEventType.RUN_FAILED,
                    timestamp,
                    runId,
                    error.nodeId,
                    message = error.buildFormattedTraceLog(),
                )
            }
        }

    private companion object {
        const val MAX_RETAINED_EVENTS = 500
    }
}
