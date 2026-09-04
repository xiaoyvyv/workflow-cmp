package com.xiaoyv.workflow.ui.platform.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberBrightnessManager(): BrightnessManager = remember {
    object : BrightnessManager {
        private var currentBrightness = 1f

        override val isSoftwareBrightness: Boolean
            get() = true

        override fun getBrightness(): Float = currentBrightness

        override fun setBrightness(level: Float) {
            currentBrightness = level.coerceIn(0.01f, 1f)
        }
    }
}
