package com.xiaoyv.workflow.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.xiaoyv.workflow.platform.video.BrightnessManager
import com.xiaoyv.workflow.platform.video.MediaVolumeManager
import com.xiaoyv.workflow.platform.video.VideoScreenController

private class AndroidSystemService(
    override val brightness: BrightnessManager,
    override val mediaVolume: MediaVolumeManager,
    override val videoScreen: VideoScreenController,
) : SystemService

@Composable
actual fun rememberSystemService(): SystemService {
    val context = LocalContext.current
    return remember(context) {
        AndroidSystemService(
            brightness = BrightnessManager(context),
            mediaVolume = MediaVolumeManager(context),
            videoScreen = VideoScreenController(context),
        )
    }
}
