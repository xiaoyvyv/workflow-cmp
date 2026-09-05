package com.xiaoyv.workflow.demo.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaoyv.workflow.demo.WorkflowsViewModel
import com.xiaoyv.workflow.editor.bridge.EditorBridgeServer
import com.xiaoyv.workflow.editor.bridge.EditorEngineRunController
import com.xiaoyv.workflow.editor.bridge.contract.EditorDeviceDescriptor
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.editor.bridge.startEditorBridge
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * 全平台通用的本地 Web 编辑器 Bridge 服务控制器。
 */
class WorkflowEditorBridgeController(
    private val viewModel: WorkflowsViewModel,
) {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    fun start(): WorkflowEditorBridgeState {
        if (server != null) return WorkflowEditorBridgeState.running()

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
            onSuccess = { WorkflowEditorBridgeState.running() },
            onFailure = { error -> WorkflowEditorBridgeState.failure(error.message.orEmpty()) },
        )
    }

    fun stop(): WorkflowEditorBridgeState {
        val runningServer = server ?: return WorkflowEditorBridgeState.stopped
        return runCatching {
            runningServer.stop(
                gracePeriodMillis = EDITOR_BRIDGE_STOP_GRACE_PERIOD_MILLIS,
                timeoutMillis = EDITOR_BRIDGE_STOP_TIMEOUT_MILLIS,
            )
            server = null
            WorkflowEditorBridgeState.stopped
        }.getOrElse { error ->
            WorkflowEditorBridgeState.failure(
                message = error.message.orEmpty(),
                isRunning = true,
            )
        }
    }
}

data class WorkflowEditorBridgeState(
    val isRunning: Boolean,
    val message: String,
    val ip: String? = null,
    val url: String = "http://localhost:$EDITOR_BRIDGE_PORT",
) {
    companion object {
        val stopped = WorkflowEditorBridgeState(
            isRunning = false,
            message = "服务未启动",
        )

        fun running(ip: String? = getDeviceIpV4()): WorkflowEditorBridgeState {
            val displayIp = ip?.takeIf { it.isNotBlank() } ?: "localhost"
            val targetUrl = "http://$displayIp:$EDITOR_BRIDGE_PORT"
            return WorkflowEditorBridgeState(
                isRunning = true,
                ip = ip,
                url = targetUrl,
                message = if (!ip.isNullOrBlank()) {
                    "运行中：$targetUrl\n(局域网设备与电脑浏览器可直接输入此地址访问)"
                } else {
                    "运行中：$targetUrl"
                },
            )
        }

        fun failure(
            message: String,
            isRunning: Boolean = false,
        ) =
            WorkflowEditorBridgeState(
                isRunning = isRunning,
                message = "操作失败：${message.ifBlank { EDITOR_BRIDGE_UNKNOWN_ERROR }}",
            )
    }
}

/**
 * 全平台通用的 Web 编辑器服务可拖拽悬浮控制面板。
 * 支持自由拖拽移动、显示当前局域网 IPv4 访问地址、一键复制、展开/折叠胶囊切换及服务启停控制。
 */
@Composable
fun WorkflowEditorBridgePanel(
    viewModel: WorkflowsViewModel,
    modifier: Modifier = Modifier,
) {
    val bridgeController = remember(viewModel) { WorkflowEditorBridgeController(viewModel) }
    var bridgeState by remember { mutableStateOf(WorkflowEditorBridgeState.stopped) }
    var isChangingBridgeState by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    DisposableEffect(bridgeController) {
        onDispose { bridgeController.stop() }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (!isExpanded) {
            // 折叠状态：紧凑型浮动胶囊
            val displayPillText = if (bridgeState.isRunning) {
                val ip = bridgeState.ip
                if (!ip.isNullOrBlank()) "Web 服务 ($ip)" else "Web 服务 (8080)"
            } else {
                "Web 服务 (未启动)"
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isExpanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (bridgeState.isRunning) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                    )
                    Text(
                        text = displayPillText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "展开",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // 展开状态：完整浮动卡片
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 4.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.widthIn(min = 260.dp, max = 330.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 顶部标题栏与最小化按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (bridgeState.isRunning) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                            )
                            Text(
                                text = "Web 编辑器服务",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { isExpanded = false }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "—",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 运行中地址与一键复制栏
                    if (bridgeState.isRunning) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(bridgeState.url))
                                    copyNotice = "已复制地址"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = bridgeState.url,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = copyNotice ?: "点击复制",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (copyNotice != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 服务描述或局域网提示
                    Text(
                        text = bridgeState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bridgeState.isRunning) {
                            FilledTonalButton(
                                enabled = !isChangingBridgeState,
                                onClick = {
                                    isChangingBridgeState = true
                                    copyNotice = null
                                    scope.launch {
                                        bridgeState = withContext(Dispatchers.Default) {
                                            bridgeController.stop()
                                        }
                                        isChangingBridgeState = false
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isChangingBridgeState) "停止中..." else "停止服务",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            Button(
                                enabled = !isChangingBridgeState,
                                onClick = {
                                    isChangingBridgeState = true
                                    copyNotice = null
                                    scope.launch {
                                        bridgeState = withContext(Dispatchers.Default) {
                                            bridgeController.start()
                                        }
                                        isChangingBridgeState = false
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isChangingBridgeState) "启动中..." else "启动服务",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val EDITOR_BRIDGE_HOST = "0.0.0.0"
private const val EDITOR_BRIDGE_PORT = 8080
private const val EDITOR_BRIDGE_DEVICE_NAME = "WorkflowCMP Device"
private const val EDITOR_BRIDGE_UNKNOWN_ERROR = "未知错误"
private const val EDITOR_BRIDGE_STOP_GRACE_PERIOD_MILLIS = 1_000L
private const val EDITOR_BRIDGE_STOP_TIMEOUT_MILLIS = 3_000L
