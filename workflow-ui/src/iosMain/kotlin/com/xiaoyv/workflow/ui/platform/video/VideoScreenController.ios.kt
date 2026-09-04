package com.xiaoyv.workflow.ui.platform.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberVideoScreenController(): VideoScreenController = remember {
    object : VideoScreenController {
        override fun enterPortrait() = Unit

        override fun enterFullscreen() = Unit
    }
}
