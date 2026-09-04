package com.xiaoyv.workflow.platform.video

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AndroidVideoScreenController(
    private val activity: Activity?,
) : VideoScreenController {
    override fun enterPortrait() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun enterFullscreen() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

fun VideoScreenController(activity: Activity?): VideoScreenController =
    AndroidVideoScreenController(activity)

fun VideoScreenController(context: Context): VideoScreenController =
    AndroidVideoScreenController(context.findActivity())
