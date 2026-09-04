package com.xiaoyv.workflow.ui.all

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.xiaoyv.workflow.ui.core.WorkflowSideEffectHost
import com.xiaoyv.workflow.ui.core.WorkflowSideEffectHostState
import com.xiaoyv.workflow.ui.image.WorkflowImagePreviewPage
import com.xiaoyv.workflow.ui.video.WorkflowVideoPreviewPage
import com.xiaoyv.workflow.ui.webview.WorkflowSyncCookieBottomSheetDialog
import com.xiaoyv.workflow.ui.webview.WorkflowWebScreen
import kotlinx.coroutines.launch

private sealed interface WorkflowDefaultPage {
    data class Image(
        val index: Int,
        val images: List<String>,
        val headers: Map<String, String>,
    ) : WorkflowDefaultPage

    data class Video(
        val url: String,
        val headers: Map<String, String>,
    ) : WorkflowDefaultPage

    data class Web(
        val url: String,
        val headers: Map<String, String>,
    ) : WorkflowDefaultPage
}

/**
 * 聚合模块的完整默认 SideEffect 宿主。
 *
 * 内置处理确认、输入、选择、进度、剪贴板、触觉、图片、视频、网页与 Cookie 同步；
 * 系统分享和通知通过消息提示确认执行请求，应用可按需在外层替换为平台实现。
 */
@Composable
fun WorkflowDefaultSideEffectHost(
    hostState: WorkflowSideEffectHostState,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf<WorkflowDefaultPage?>(null) }

    Box(modifier = modifier) {
        WorkflowSideEffectHost(
            hostState = hostState,
            onShowToast = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            },
            onWriteClipboard = { text ->
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
            },
            onReadClipboard = {
                clipboardManager.getText()?.text
            },
            onOpenExternalUrl = { url ->

            },
            onOpenExternalApp = { uri, fallbackUrl ->
                val url = fallbackUrl ?: uri

            },
            onOpenInternalWeb = { url, headers ->
                page = WorkflowDefaultPage.Web(url, headers)
            },
            onImagePreview = { index, images, headers ->
                page = WorkflowDefaultPage.Image(index, images, headers)
            },
            onVideoPreview = { url, headers ->
                page = WorkflowDefaultPage.Video(url, headers)
            },
            onShareText = { _, text, url ->
                scope.launch {
                    snackbarHostState.showSnackbar(if (url.isBlank()) text else "$text\n$url")
                }
            },
            onSendNotification = { title, content ->
                scope.launch {
                    snackbarHostState.showSnackbar(listOf(title, content).filter(String::isNotBlank).joinToString("\n"))
                }
            },
            onVibrate = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            confirmDialogSlot = { effect, onConfirm, onCancel ->
                WorkflowConfirmAlertDialog(
                    effect = effect,
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                )
            },
            inputDialogSlot = { effect, onConfirm, onCancel ->
                WorkflowInputAlertDialog(
                    effect = effect,
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                )
            },
            selectDialogSlot = { effect, onConfirm, onCancel ->
                WorkflowSelectAlertDialog(
                    effect = effect,
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                )
            },
            syncCookieDialogSlot = { effect, onComplete ->
                WorkflowSyncCookieBottomSheetDialog(
                    effect = effect,
                    onComplete = onComplete,
                )
            },
        )

        WorkflowProgressAlertDialog(tasks = hostState.progressTasks)

        SnackbarHost(hostState = snackbarHostState)

        val platformContext = LocalPlatformContext.current

        when (val currentPage = page) {
            is WorkflowDefaultPage.Image -> WorkflowImagePreviewPage(
                images = remember(currentPage.images, platformContext) {
                    currentPage.images.map {
                        ImageRequest.Builder(platformContext)
                            .data(it)
                            .build()
                    }
                },
                initialIndex = currentPage.index,
                onDismiss = { page = null },
                modifier = Modifier.fillMaxSize(),
            )

            is WorkflowDefaultPage.Video -> WorkflowVideoPreviewPage(
                videoUrl = currentPage.url,
                onDismiss = { page = null },
                modifier = Modifier.fillMaxSize(),
            )

            is WorkflowDefaultPage.Web -> WorkflowWebScreen(
                url = currentPage.url,
                modifier = Modifier.fillMaxSize(),
                onDismiss = { page = null },
            )

            null -> Unit
        }
    }
}
