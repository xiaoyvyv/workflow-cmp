package com.xiaoyv.workflow.platform.video

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIScreen
import platform.UIKit.UIScreenBrightnessDidChangeNotification

@OptIn(ExperimentalForeignApi::class)
class IosBrightnessManager : BrightnessManager {
    /**
     * 系统亮度写入不会立即反映到读取结果，因此手势期间使用缓存值。
     */
    private var brightness = UIScreen.mainScreen.brightness.toFloat()

    private val brightnessObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = UIScreenBrightnessDidChangeNotification,
        `object` = null,
        queue = null,
    ) {
        brightness = UIScreen.mainScreen.brightness.toFloat()
    }

    override fun getBrightness(): Float = brightness

    override fun setBrightness(level: Float) {
        val savedLevel = level.coerceIn(0.01f, 1f)
        brightness = savedLevel
        UIScreen.mainScreen.brightness = savedLevel.toDouble()
    }

    override fun close() {
        NSNotificationCenter.defaultCenter.removeObserver(brightnessObserver)
    }
}

fun BrightnessManager(): BrightnessManager = IosBrightnessManager()
