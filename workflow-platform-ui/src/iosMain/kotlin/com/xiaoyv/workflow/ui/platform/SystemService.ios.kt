package com.xiaoyv.workflow.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.xiaoyv.workflow.platform.video.BrightnessManager
import com.xiaoyv.workflow.platform.video.MediaVolumeManager
import com.xiaoyv.workflow.platform.video.VideoScreenController

private class IosSystemService(
    override val brightness: BrightnessManager,
    override val mediaVolume: MediaVolumeManager,
    override val videoScreen: VideoScreenController,
) : SystemService

@Composable
actual fun rememberSystemService(): SystemService {
    val brightnessManager = remember { BrightnessManager() }
    DisposableEffect(brightnessManager) {
        onDispose {
            brightnessManager.close()
        }
    }
    return remember(brightnessManager) {
        IosSystemService(
            brightness = brightnessManager,
            mediaVolume = MediaVolumeManager(),
            videoScreen = VideoScreenController(),
        )
    }
}
