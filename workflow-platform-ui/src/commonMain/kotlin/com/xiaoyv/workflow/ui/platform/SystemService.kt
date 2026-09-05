package com.xiaoyv.workflow.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.xiaoyv.workflow.platform.video.BrightnessManager
import com.xiaoyv.workflow.platform.video.MediaVolumeManager
import com.xiaoyv.workflow.platform.video.VideoScreenController

/**
 * 平台系统服务统一门面。
 *
 * 聚合系统屏幕亮度调节、媒体音量调节以及屏幕方向与全屏窗口控制器。
 */
interface SystemService {
    /**
     * 屏幕亮度管理器。
     */
    val brightness: BrightnessManager

    /**
     * 媒体音量管理器。
     */
    val mediaVolume: MediaVolumeManager

    /**
     * 视频画面屏幕方向与全屏控制器。
     */
    val videoScreen: VideoScreenController
}

/**
 * 提供系统服务的 CompositionLocal。
 */
val LocalSystemService = staticCompositionLocalOf<SystemService?> { null }

/**
 * 创建并记忆当前平台的统一系统服务。
 */
@Composable
expect fun rememberSystemService(): SystemService
