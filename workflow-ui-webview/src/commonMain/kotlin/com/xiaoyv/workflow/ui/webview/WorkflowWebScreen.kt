package com.xiaoyv.workflow.ui.webview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator

/**
 * 对应 WebScreen 的默认网页页面，包含导航器和加载进度。
 */
@Composable
fun WorkflowWebScreen(
    url: String,
    onTitleChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state = rememberSaveableWebViewState(url = url)
    val navigator = rememberWebViewNavigator()

    Box(modifier.fillMaxSize()) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            state = state,
            navigator = navigator
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
