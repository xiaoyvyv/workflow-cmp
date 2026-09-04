package com.xiaoyv.workflow.ui.platform.back

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS 没有系统级物理返回按键
}
