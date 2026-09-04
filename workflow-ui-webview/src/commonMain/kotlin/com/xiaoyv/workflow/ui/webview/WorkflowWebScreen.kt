package com.xiaoyv.workflow.ui.webview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.xiaoyv.workflow.ui.platform.back.PlatformBackHandler
import com.xiaoyv.workflow.util.serialization.SerializeMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * 对应 WebScreen 的默认网页页面，包含导航器和加载进度。
 *
 * 使用 [rememberSaveableWebViewState] 在配置变更后保留导航历史。
 * 通过 [LaunchedEffect] 仅在首次创建（viewState == null）时主动触发 loadUrl，
 * 避免 WebView 状态恢复后重复加载导致空白页。
 */
@Composable
fun WorkflowWebScreen(
    url: String,
    modifier: Modifier = Modifier,
    headers: SerializeMap<String, String> = persistentMapOf(),
    onTitleChange: (String) -> Unit = {},
    captureBackPressed: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    val state = rememberSaveableWebViewState(url = url, headers)
    val navigator = rememberWebViewNavigator()

    PlatformBackHandler(enabled = captureBackPressed) { onDismiss() }

    // 仅在首次创建时（viewState == null）主动加载，恢复状态时跳过避免空白
    LaunchedEffect(navigator) {
        if (state.viewState == null) {
            navigator.loadUrl(url, headers)
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            state = state,
            navigator = navigator,
        )

        val loading = state.loadingState
        if (loading is LoadingState.Loading) {
            val progress by animateFloatAsState(targetValue = loading.progress)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    state.pageTitle?.takeIf { it.isNotBlank() }?.let(onTitleChange)
}
