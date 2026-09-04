package com.xiaoyv.workflow.demo

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaoyv.workflow.demo.business.WorkflowSamples
import com.xiaoyv.workflow.demo.support.debugLog
import com.xiaoyv.workflow.engine.ActionSideEffectDispatcher
import com.xiaoyv.workflow.engine.ActionSideEffectResult
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.execution.ActionExecutionContext
import com.xiaoyv.workflow.model.execution.ActionExecutionEvent
import com.xiaoyv.workflow.model.execution.ActionSideEffect
import com.xiaoyv.workflow.model.log.ActionExecutionStatus
import com.xiaoyv.workflow.node.effect.ActionConfirmEffect
import com.xiaoyv.workflow.node.effect.ActionImagePreviewEffect
import com.xiaoyv.workflow.node.effect.ActionInputDialogEffect
import com.xiaoyv.workflow.node.effect.ActionNotificationEffect
import com.xiaoyv.workflow.node.effect.ActionOpenExternalAppEffect
import com.xiaoyv.workflow.node.effect.ActionOpenExternalUrlEffect
import com.xiaoyv.workflow.node.effect.ActionOpenInternalWebEffect
import com.xiaoyv.workflow.node.effect.ActionReadClipboardEffect
import com.xiaoyv.workflow.node.effect.ActionSelectDialogEffect
import com.xiaoyv.workflow.node.effect.ActionShareEffect
import com.xiaoyv.workflow.node.effect.ActionShowToastEffect
import com.xiaoyv.workflow.node.effect.ActionSyncCookieEffect
import com.xiaoyv.workflow.node.effect.ActionVideoPreviewEffect
import com.xiaoyv.workflow.node.effect.ActionWriteClipboardEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

const val WORKFLOW_RUN_STATUS_RUNNING = "running"

/**
 * 示例工作流最近一次执行的可展示状态。
 */
@Immutable
data class WorkflowExampleRunState(
    val workflowId: String? = null,
    val status: String,
    val executedNodeIds: List<String> = emptyList(),
    val resolvedUrl: String? = null,
    val errorMessage: String? = null,
    val outputLines: List<String> = emptyList(),
)

/**
 * 测试工作流分类枚举。
 */
enum class WorkflowCategory {
    ALL,
    PRACTICE,
    FLOW_CONTROL,
    DATA_MATH,
    TEXT_NET,
    UI_SIDE_EFFECT,
    ERROR_TEST;

    fun matches(workflow: ActionWorkflow): Boolean = when (this) {
        ALL -> true
        PRACTICE -> workflow.id in setOf(
            "subject_tags_to_toast",
            "bilibili_wbi_search",
            "mangadex_search",
            "hanime_search",
        )

        FLOW_CONTROL -> workflow.nodes.any {
            it.type.startsWith("flow.") || it.type.startsWith("control.")
        }

        DATA_MATH -> workflow.nodes.any {
            it.type.startsWith("data.") || it.type.startsWith("object.") ||
                    it.type.startsWith("array.") || it.type.startsWith("math.")
        }

        TEXT_NET -> workflow.nodes.any {
            it.type.startsWith("text.") || it.type.startsWith("html.") ||
                    it.type.startsWith("json.") || it.type.startsWith("xml.") ||
                    it.type.startsWith("csv.") || it.type.startsWith("url.") ||
                    it.type.startsWith("crypto.") || it.type.startsWith("codec.") ||
                    it.type.startsWith("date.")
        }

        UI_SIDE_EFFECT -> workflow.nodes.any {
            it.type.startsWith("action.") || it.type.startsWith("ui.") ||
                    it.type.startsWith("system.") || it.type.startsWith("storage.") ||
                    it.type.startsWith("bilibili.")
        }

        ERROR_TEST -> workflow.id.contains("error") ||
                workflow.name.contains("错误") ||
                workflow.name.contains("异常") ||
                workflow.name.contains("故障") ||
                workflow.name.contains("失败") ||
                workflow.name.contains("越界")
    }
}

/**
 * 工作流测试页 UI 状态。
 */
@Immutable
data class WorkflowsUiState(
    val selectedCategory: WorkflowCategory = WorkflowCategory.ALL,
    val selectedPanelTab: Int = 0, // 0: 节点卡片, 1: 运行日志
    val currentRun: WorkflowExampleRunState? = null,
)

/**
 * MVI 用户意图契约。
 */
sealed interface WorkflowsIntent {
    data class SelectCategory(val category: WorkflowCategory) : WorkflowsIntent
    data class SelectPanelTab(val tab: Int) : WorkflowsIntent
    data class RunSample(val sampleId: String) : WorkflowsIntent
    data class OnSideEffectResult(val id: String, val result: ActionSideEffectResult) : WorkflowsIntent
}

/**
 * MVI 单次副作用契约。
 */
sealed interface WorkflowsSideEffect {
    data class Action(val id: String, val effect: ActionSideEffect) : WorkflowsSideEffect
    data class Toast(val message: String) : WorkflowsSideEffect
}

/**
 * 轻量级、无三方依赖的 MVI ViewModel。
 */
class WorkflowsViewModel(
    private val engine: ActionWorkflowEngine,
) : ViewModel() {

    private val activeSideEffectDispatcher = ActionSideEffectDispatcher()

    private val _uiState = MutableStateFlow(WorkflowsUiState())
    val uiState: StateFlow<WorkflowsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<WorkflowsSideEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<WorkflowsSideEffect> = _uiEffect.receiveAsFlow()

    private fun emitUiEffect(effect: WorkflowsSideEffect) {
        _uiEffect.trySend(effect)
    }

    fun onIntent(intent: WorkflowsIntent) {
        when (intent) {
            is WorkflowsIntent.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = intent.category) }
            }

            is WorkflowsIntent.SelectPanelTab -> {
                _uiState.update { it.copy(selectedPanelTab = intent.tab) }
            }

            is WorkflowsIntent.RunSample -> {
                runSample(intent.sampleId)
            }

            is WorkflowsIntent.OnSideEffectResult -> {
                activeSideEffectDispatcher.complete(intent.id, intent.result)
            }
        }
    }

    private fun runSample(sampleId: String) {
        val sample = WorkflowSamples.find(sampleId) ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentRun = WorkflowExampleRunState(
                        workflowId = sample.id,
                        status = WORKFLOW_RUN_STATUS_RUNNING,
                        outputLines = listOf("🚀 启动工作流 [${sample.id}]"),
                    ),
                )
            }

            engine.execute(
                workflow = sample,
                initialContext = ActionExecutionContext(
                    input = buildJsonObject {
                        put("nameCn", JsonPrimitive("孤独摇滚"))
                        put("id", JsonPrimitive(328609))
                        put("isFavorite", JsonPrimitive(true))
                    },
                    environment = buildJsonObject { put("locale", JsonPrimitive("zh-CN")) },
                    trigger = buildJsonObject { put("source", JsonPrimitive("workflows_screen")) },
                ),
                sideEffectHandler = { effect ->
                    val effectId = ActionSideEffectDispatcher.generateSideEffectId()
                    when (effect) {
                        is ActionShowToastEffect -> {
                            emitUiEffect(WorkflowsSideEffect.Toast(effect.message))
                            ActionSideEffectResult.Success()
                        }

                        else -> {
                            activeSideEffectDispatcher.awaitUiResult(effectId) {
                                emitUiEffect(WorkflowsSideEffect.Action(effectId, effect))
                            }
                        }
                    }
                },
            ).collect { event ->
                val output = event.toOutputLine()
                debugLog {
                    tag = "ActionWorkflow"
                    output
                }
                _uiState.update { state ->
                    val run = state.currentRun
                    val updatedRun = when (event) {
                        is ActionExecutionEvent.Started -> WorkflowExampleRunState(
                            workflowId = event.workflowId,
                            status = WORKFLOW_RUN_STATUS_RUNNING,
                            outputLines = listOf(output),
                        )

                        is ActionExecutionEvent.NodeStarted -> run?.copy(
                            outputLines = run.outputLines + output,
                        )

                        is ActionExecutionEvent.NodeCompleted -> run?.copy(
                            executedNodeIds = run.executedNodeIds + event.nodeId,
                            outputLines = run.outputLines + output,
                        )

                        is ActionExecutionEvent.SideEffectRequested -> run?.copy(
                            resolvedUrl = (event.effect as? ActionOpenExternalUrlEffect)?.url ?: run.resolvedUrl,
                            outputLines = run.outputLines + output,
                        )

                        is ActionExecutionEvent.Failed -> run?.copy(
                            status = ActionExecutionStatus.FAILED,
                            errorMessage = event.error.message,
                            outputLines = run.outputLines + output,
                        )

                        is ActionExecutionEvent.Completed -> run?.copy(
                            status = event.log.status,
                            outputLines = run.outputLines + output,
                        )
                    }
                    state.copy(currentRun = updatedRun)
                }
            }
        }
    }

    private fun ActionExecutionEvent.toOutputLine(): String = when (this) {
        is ActionExecutionEvent.Started -> "🚀 启动工作流 [$workflowId]"
        is ActionExecutionEvent.NodeStarted -> "▶️ 准备执行节点: $nodeId"
        is ActionExecutionEvent.NodeCompleted -> {
            val dataStr = if (output.isEmpty()) "" else " → 输出: $output"
            "✅ 节点 [$nodeId] 执行完成 (出口: $outputPortId)$dataStr"
        }

        is ActionExecutionEvent.SideEffectRequested -> "⚡ 触发系统动作 [$nodeId]: ${formatSideEffect(effect)}"
        is ActionExecutionEvent.Failed -> "❌ 执行发生异常: ${error.message}"
        is ActionExecutionEvent.Completed -> {
            val duration = (log.finishedAt - log.startedAt).coerceAtLeast(0)
            "🏁 工作流运行结束 (状态: ${log.status}, 共 ${log.steps.size} 步, 耗时: ${duration}ms)"
        }
    }

    private fun formatSideEffect(effect: ActionSideEffect): String = when (effect) {
        is ActionShowToastEffect -> "弹窗提示 \"${effect.message}\""
        is ActionWriteClipboardEffect -> "写入剪贴板 \"${effect.text}\""
        is ActionOpenExternalUrlEffect -> "打开外部网页 ${effect.url}"
        is ActionOpenExternalAppEffect -> "唤起应用 ${effect.uri}"
        is ActionOpenInternalWebEffect -> "打开内置网页 ${effect.url}"
        is ActionConfirmEffect -> "二次确认框 \"${effect.message}\""
        is ActionShareEffect -> "系统分享 \"${effect.text}\""
        is ActionNotificationEffect -> "发送通知 \"${effect.content}\""
        is ActionImagePreviewEffect -> "预览图片 (共 ${effect.images.size} 张, 当前: ${effect.index})"
        is ActionVideoPreviewEffect -> "预览视频 ${effect.url}"
        is ActionSyncCookieEffect -> "同步 Cookie ${effect.url}"
        is ActionReadClipboardEffect -> "读取剪贴板"
        is ActionInputDialogEffect -> "输入弹窗 \"${effect.title}\""
        is ActionSelectDialogEffect -> "选择弹窗 \"${effect.title}\""
        else -> effect::class.simpleName.orEmpty()
    }
}
