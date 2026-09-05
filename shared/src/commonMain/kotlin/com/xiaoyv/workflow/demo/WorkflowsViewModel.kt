package com.xiaoyv.workflow.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaoyv.workflow.demo.business.WorkflowSamples
import com.xiaoyv.workflow.demo.business.samples.ActionSamples
import com.xiaoyv.workflow.demo.business.samples.BilibiliSamples
import com.xiaoyv.workflow.demo.business.samples.BusinessSamples
import com.xiaoyv.workflow.demo.business.samples.CodecSamples
import com.xiaoyv.workflow.demo.business.samples.CryptoSamples
import com.xiaoyv.workflow.demo.business.samples.DataSamples
import com.xiaoyv.workflow.demo.business.samples.ErrorSamples
import com.xiaoyv.workflow.demo.business.samples.FlowSamples
import com.xiaoyv.workflow.demo.business.samples.HtmlSamples
import com.xiaoyv.workflow.demo.business.samples.IoSamples
import com.xiaoyv.workflow.demo.support.debugLog
import com.xiaoyv.workflow.di.WorkflowRuntime
import com.xiaoyv.workflow.di.WorkflowRuntimeConfig
import com.xiaoyv.workflow.di.createWorkflowRuntime
import com.xiaoyv.workflow.engine.ActionSideEffectDispatcher
import com.xiaoyv.workflow.engine.ActionSideEffectHandler
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
import com.xiaoyv.workflow.platform.room.cookie.RoomActionCookiesStorage
import com.xiaoyv.workflow.port.impl.DefaultActionHttpRequestExecutor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
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
    val isSuccess: Boolean? = null,
    val resolvedUrl: String? = null,
    val errorMessage: String? = null,
    val outputLines: List<String> = emptyList(),
)

/**
 * 测试工作流分类枚举：按业务示例、节点模块与错误测试分组。
 */
enum class WorkflowCategory(val label: String) {
    EXAMPLES("示例工作流"),
    NODE_CONTROL("control"),
    NODE_DATA("data"),
    NODE_CODEC("codec"),
    NODE_HTML("html"),
    NODE_CRYPTO("crypto"),
    NODE_IO("io"),
    NODE_ACTION("action"),
    NODE_BILIBILI("bilibili"),
    ERROR_TEST("错误测试");

    fun samples(): List<ActionWorkflow> = when (this) {
        EXAMPLES -> BusinessSamples.all
        NODE_CONTROL -> FlowSamples.all
        NODE_DATA -> DataSamples.all
        NODE_CODEC -> CodecSamples.all
        NODE_HTML -> HtmlSamples.all
        NODE_CRYPTO -> CryptoSamples.all
        NODE_IO -> IoSamples.all
        NODE_ACTION -> ActionSamples.all
        NODE_BILIBILI -> BilibiliSamples.all
        ERROR_TEST -> ErrorSamples.all
    }
}

/**
 * 工作流测试页 UI 状态。
 */
@Immutable
data class WorkflowsUiState(
    val selectedCategory: WorkflowCategory = WorkflowCategory.EXAMPLES,
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
    val runtime: WorkflowRuntime,
    private val httpClient: HttpClient? = null,
) : ViewModel() {

    private val engine: ActionWorkflowEngine = runtime.engine

    private val activeSideEffectDispatcher = ActionSideEffectDispatcher()

    private val _uiState = MutableStateFlow(WorkflowsUiState())
    val uiState: StateFlow<WorkflowsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<WorkflowsSideEffect>(Channel.UNLIMITED)
    val uiEffect: Flow<WorkflowsSideEffect> = _uiEffect.receiveAsFlow()

    fun close() {
        httpClient?.close()
    }

    override fun onCleared() {
        close()
    }

    private fun emitUiEffect(effect: WorkflowsSideEffect) {
        _uiEffect.trySend(effect)
    }

    val sideEffectHandler: ActionSideEffectHandler = { effect ->
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
                sideEffectHandler = sideEffectHandler,
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

/**
 * 创建桌面与移动端示例共用的 ViewModel 和真实工作流运行时。
 */
fun createWorkflowsViewModel(): WorkflowsViewModel {
    val cookiesStorage = RoomActionCookiesStorage()
    val httpClient =
        HttpClient {
            install(HttpCookies) {
                storage = cookiesStorage
            }
            install(Logging) {
                level = LogLevel.ALL
                format = LoggingFormat.OkHttp
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("[Network] $message")
                    }
                }
            }
        }
    val runtime =
        createWorkflowRuntime(
            config =
                WorkflowRuntimeConfig(
                    httpRequestExecutor = DefaultActionHttpRequestExecutor(httpClient),
                ),
        )
    return WorkflowsViewModel(runtime, httpClient)
}

/**
 * 在组合生命周期内创建并释放应用唯一的工作流 ViewModel。
 */
@Composable
fun rememberWorkflowsViewModel(): WorkflowsViewModel {
    val viewModel = remember { createWorkflowsViewModel() }

    DisposableEffect(viewModel) {
        onDispose { viewModel.close() }
    }

    return viewModel
}
