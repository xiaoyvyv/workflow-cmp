package com.xiaoyv.workflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.xiaoyv.workflow.demo.WorkflowsViewModel
import com.xiaoyv.workflow.demo.rememberWorkflowsViewModel
import com.xiaoyv.workflow.editor.bridge.contract.EditorDeviceDescriptor
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.editor.bridge.jvm.EditorBridgeServer
import com.xiaoyv.workflow.editor.bridge.jvm.EditorEngineRunController
import com.xiaoyv.workflow.editor.bridge.jvm.startEditorBridge
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workflowcmp",
    ) {
        val viewModel = rememberWorkflowsViewModel()
        val bridgeController = remember(viewModel) { DesktopEditorBridgeController(viewModel) }
        var bridgeState by remember { mutableStateOf(DesktopEditorBridgeState.stopped) }
        var isChangingBridgeState by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        DisposableEffect(bridgeController) {
            onDispose { bridgeController.stop() }
        }

        Box {
            App(viewModel)
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(EDITOR_BRIDGE_PANEL_PADDING),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = EDITOR_BRIDGE_PANEL_ELEVATION,
            ) {
                Column(modifier = Modifier.padding(EDITOR_BRIDGE_PANEL_CONTENT_PADDING)) {
                    Text(
                        text = "Web 编辑器服务",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = bridgeState.message,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row {
                        Button(
                            enabled = !bridgeState.isRunning && !isChangingBridgeState,
                            onClick = {
                                isChangingBridgeState = true
                                scope.launch {
                                    bridgeState =
                                        withContext(Dispatchers.IO) {
                                            bridgeController.start()
                                        }
                                    isChangingBridgeState = false
                                }
                            },
                        ) {
                            Text("启动")
                        }
                        Button(
                            enabled = bridgeState.isRunning && !isChangingBridgeState,
                            onClick = {
                                isChangingBridgeState = true
                                scope.launch {
                                    bridgeState =
                                        withContext(Dispatchers.IO) {
                                            bridgeController.stop()
                                        }
                                    isChangingBridgeState = false
                                }
                            },
                            modifier = Modifier.padding(start = EDITOR_BRIDGE_BUTTON_SPACING),
                        ) {
                            Text("停止")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 为桌面演示提供可选的本地编辑器服务，不承担工作流实际执行。
 */
private class DesktopEditorBridgeController(
    private val viewModel: WorkflowsViewModel,
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    fun start(): DesktopEditorBridgeState {
        if (server != null) return DesktopEditorBridgeState.running

        return runCatching {
            val service =
                EditorWorkflowService(
                    registry = viewModel.runtime.registry,
                    validator = viewModel.runtime.validator,
                    repository = InMemoryEditorWorkflowRepository(),
                    manifestCodec = viewModel.runtime.editorManifestCodec,
                )
            val runController =
                EditorEngineRunController(
                    service = service,
                    engine = viewModel.runtime.engine,
                    sideEffectHandler = viewModel.sideEffectHandler,
                    scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                )
            val bridge =
                EditorBridgeServer(
                    service = service,
                    device = EditorDeviceDescriptor(name = EDITOR_BRIDGE_DEVICE_NAME),
                    json = viewModel.runtime.config.json,
                    runController = runController,
                )
            startEditorBridge(
                bridge = bridge,
                host = EDITOR_BRIDGE_HOST,
                port = EDITOR_BRIDGE_PORT,
            ).also { server = it }
        }.fold(
            onSuccess = { DesktopEditorBridgeState.running },
            onFailure = { error -> DesktopEditorBridgeState.failure(error.message.orEmpty()) },
        )
    }

    fun stop(): DesktopEditorBridgeState {
        val runningServer = server ?: return DesktopEditorBridgeState.stopped
        return runCatching {
            runningServer.stop(
                gracePeriodMillis = EDITOR_BRIDGE_STOP_GRACE_PERIOD_MILLIS,
                timeoutMillis = EDITOR_BRIDGE_STOP_TIMEOUT_MILLIS,
            )
            server = null
            DesktopEditorBridgeState.stopped
        }.getOrElse { error ->
            DesktopEditorBridgeState.failure(
                message = error.message.orEmpty(),
                isRunning = true,
            )
        }
    }
}

private data class DesktopEditorBridgeState(
    val isRunning: Boolean,
    val message: String,
) {
    companion object {
        val stopped = DesktopEditorBridgeState(false, "服务未启动")
        val running = DesktopEditorBridgeState(true, "运行中：$EDITOR_BRIDGE_URL")

        fun failure(
            message: String,
            isRunning: Boolean = false,
        ) =
            DesktopEditorBridgeState(
                isRunning = isRunning,
                message = "操作失败：${message.ifBlank { EDITOR_BRIDGE_UNKNOWN_ERROR }}",
            )
    }
}

private const val EDITOR_BRIDGE_HOST = "127.0.0.1"
private const val EDITOR_BRIDGE_PORT = 8080
private const val EDITOR_BRIDGE_URL = "http://$EDITOR_BRIDGE_HOST:$EDITOR_BRIDGE_PORT"
private const val EDITOR_BRIDGE_DEVICE_NAME = "WorkflowCMP Desktop"
private const val EDITOR_BRIDGE_UNKNOWN_ERROR = "未知错误"
private const val EDITOR_BRIDGE_STOP_GRACE_PERIOD_MILLIS = 1_000L
private const val EDITOR_BRIDGE_STOP_TIMEOUT_MILLIS = 3_000L

private val EDITOR_BRIDGE_PANEL_PADDING = 16.dp
private val EDITOR_BRIDGE_PANEL_CONTENT_PADDING = 12.dp
private val EDITOR_BRIDGE_PANEL_ELEVATION = 6.dp
private val EDITOR_BRIDGE_BUTTON_SPACING = 8.dp
