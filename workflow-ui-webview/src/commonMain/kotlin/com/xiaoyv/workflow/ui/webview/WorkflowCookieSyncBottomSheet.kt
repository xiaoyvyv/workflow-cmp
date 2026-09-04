package com.xiaoyv.workflow.ui.webview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.cookie.WebViewCookieManager
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.xiaoyv.workflow.node.effect.ActionSyncCookieEffect
import kotlinx.coroutines.launch

/**
 * 从嵌入式 WebView 取回的 Cookie 数据。
 */
data class WorkflowWebCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
)

/**
 * 默认 Cookie 同步页面。
 *
 * 用户关闭 BottomSheet 后读取 WebView Cookie 并回传给宿主；持久化策略由调用方决定，
 * 因此 UI 模块不会耦合任何特定应用的数据层。
 */
@Composable
fun WorkflowCookieSyncBottomSheet(
    effect: ActionSyncCookieEffect,
    onComplete: suspend (List<WorkflowWebCookie>) -> Unit,
) {
    val state = rememberWebViewState(
        url = effect.url,
        additionalHttpHeaders = effect.headers,
        extraSettings = {
            customUserAgentString = effect.userAgent
        },
    )
    val navigator = rememberWebViewNavigator()
    val cookieManager = remember { WebViewCookieManager() }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                onComplete(
                    cookieManager.getCookies(effect.url).map { cookie ->
                        WorkflowWebCookie(
                            name = cookie.name,
                            value = cookie.value,
                            domain = cookie.domain.orEmpty(),
                            path = cookie.path.orEmpty(),
                        )
                    },
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = effect.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                text = effect.url,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                WebView(
                    state = state,
                    navigator = navigator,
                    modifier = Modifier.fillMaxSize(),
                    captureBackPresses = false,
                )
                val loadingState = state.loadingState
                if (loadingState is LoadingState.Loading) {
                    val progress by animateFloatAsState(targetValue = loadingState.progress)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
