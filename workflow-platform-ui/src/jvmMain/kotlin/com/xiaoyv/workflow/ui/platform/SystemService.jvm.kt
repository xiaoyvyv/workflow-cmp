package com.xiaoyv.workflow.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.xiaoyv.workflow.platform.video.BrightnessManager
import com.xiaoyv.workflow.platform.video.MediaVolumeManager
import com.xiaoyv.workflow.platform.video.VideoScreenController

private class JvmSystemService(
    override val brightness: BrightnessManager,
    override val mediaVolume: MediaVolumeManager,
    override val videoScreen: VideoScreenController,
) : SystemService

@Composable
actual fun rememberSystemService(): SystemService = remember {
    JvmSystemService(
        brightness = BrightnessManager(),
        mediaVolume = MediaVolumeManager(),
        videoScreen = VideoScreenController(),
    )
}
