package com.xiaoyv.workflow.ui.webview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multiplatform.webview.cookie.WebViewCookieManager
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.xiaoyv.workflow.node.effect.ActionSyncCookieEffect
import com.xiaoyv.workflow.ui.core.layout.WorkflowContentMargin
import com.xiaoyv.workflow.ui.core.layout.WorkflowContentMarginHalf
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.launch

val LocalSyncWebCookieStorge = staticCompositionLocalOf<CookiesStorage> {
    error("LocalSyncWebCookieStorge not provided")
}

/**
 * 工作流内置的网页 Cookie 同步 BottomSheet 弹窗。
 *
 * 弹出时以 BottomSheet 展现网页同步界面，点击确定或关闭操作后，均会回调触发 Cookie 同步与弹窗关闭。
 *
 * @param effect Cookie 同步 SideEffect 数据模型
 * @param onComplete 划走关闭回调（触发 Cookie 同步与出队）
 */
@Composable
fun WorkflowSyncCookieBottomSheetDialog(
    effect: ActionSyncCookieEffect,
    onComplete: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(initialValue = Hidden, setOf(Hidden, Expanded))
    var isTouchInsideWebView by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cookiesStorage = LocalSyncWebCookieStorge.current

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        onDismissRequest = {
            coroutineScope.launch {
                val url = URLBuilder(effect.url).apply { encodedPath = "/" }.build()
                val webViewCookieManager = WebViewCookieManager()
                val cookies = webViewCookieManager.getCookies(url.toString())
                cookies.forEach { cookie ->
                    cookiesStorage.addCookie(
                        requestUrl = url,
                        cookie = Cookie(
                            name = cookie.name,
                            value = cookie.value,
                            path = cookie.path,
                            domain = cookie.domain,
                            secure = cookie.isSecure == true,
                            httpOnly = cookie.isHttpOnly == true,
                            expires = GMTDate(cookie.expiresDate),
                        )
                    )
                }
                onComplete()
            }
        },
        sheetState = sheetState,
        sheetGesturesEnabled = !isTouchInsideWebView,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true,
            shouldDismissOnClickOutside = false
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WorkflowContentMargin, vertical = WorkflowContentMarginHalf)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Cookie Sync：" + effect.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = WorkflowContentMarginHalf),
                    text = effect.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = WorkflowContentMarginHalf, bottom = WorkflowContentMarginHalf),
                        text = "适用于处理人机验证、登录网页等，让后续请求（请求节点需声明启用Cookie）持有网页的身份和验证信息，操作完成关闭即可",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val anyPressed = event.changes.any { it.pressed }
                                if (isTouchInsideWebView != anyPressed) {
                                    isTouchInsideWebView = anyPressed
                                }
                            }
                        }
                    }
            ) {
                val navigator = rememberWebViewNavigator()
                val state = rememberWebViewState(
                    url = effect.url,
                    additionalHttpHeaders = effect.headers,
                    extraSettings = {
                        customUserAgentString = effect.userAgent
                    }
                )

                WebView(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    captureBackPresses = false,
                    navigator = navigator
                )

                val loadingState = state.loadingState
                if (loadingState is LoadingState.Loading) {
                    val progress by animateFloatAsState(
                        targetValue = loadingState.progress,
                        animationSpec = tween(),
                    )

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { progress },
                        gapSize = 0.dp,
                        strokeCap = StrokeCap.Square,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}
