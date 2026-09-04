package com.xiaoyv.workflow.platform.video

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings

class AndroidBrightnessManager(
    private val context: Context,
) : BrightnessManager {
    private val activity = context.findActivity()

    override fun getBrightness(): Float {
        val windowBrightness = activity?.window?.attributes?.screenBrightness
        if (windowBrightness != null && windowBrightness >= 0f) return windowBrightness
        val systemBrightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            DefaultSystemBrightness,
        )
        return systemBrightness / MaxSystemBrightness.toFloat()
    }

    override fun setBrightness(level: Float) {
        activity?.window?.attributes = activity.window.attributes.apply {
            screenBrightness = level.coerceIn(MinBrightness, MaxBrightness)
        }
    }

    private companion object {
        const val DefaultSystemBrightness = 128
        const val MaxSystemBrightness = 255
        const val MinBrightness = 0.01f
        const val MaxBrightness = 1f
    }
}

fun BrightnessManager(context: Context): BrightnessManager = AndroidBrightnessManager(context)

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
