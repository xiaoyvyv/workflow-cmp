package com.xiaoyv.workflow.platform.video

class JvmBrightnessManager : BrightnessManager {
    private var currentBrightness = 1f

    override val isSoftwareBrightness: Boolean
        get() = true

    override fun getBrightness(): Float = currentBrightness

    override fun setBrightness(level: Float) {
        currentBrightness = level.coerceIn(0.01f, 1f)
    }
}

fun BrightnessManager(): BrightnessManager = JvmBrightnessManager()
