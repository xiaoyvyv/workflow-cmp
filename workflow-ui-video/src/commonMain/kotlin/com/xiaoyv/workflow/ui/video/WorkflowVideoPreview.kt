package com.xiaoyv.workflow.ui.video

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xiaoyv.workflow.ui.platform.back.PlatformBackHandler
import com.xiaoyv.workflow.ui.video.internal.VideoScaffold
import com.xiaoyv.workflow.util.serialization.SerializeMap
import kotlinx.collections.immutable.persistentMapOf
import org.openani.mediamp.compose.rememberMediampPlayer
import org.openani.mediamp.source.MediaExtraFiles
import org.openani.mediamp.source.UriMediaData

/**
 * 对应 PreviewVideo 的默认 MediaMP 播放页面。
 */
@Composable
fun WorkflowVideoPreviewPage(
    videoUrl: String,
    modifier: Modifier = Modifier,
    headers: SerializeMap<String, String> = persistentMapOf(),
    title: String = "视频预览",
    backgroundColor: Color = Color.Black,
    captureBackPressed: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    val player = rememberMediampPlayer()

    PlatformBackHandler(enabled = captureBackPressed) { onDismiss() }

    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotBlank()) {
            player.setMediaData(
                data = UriMediaData(videoUrl, headers, MediaExtraFiles()),
                playWhenReady = true,
                startPositionMillis = 0
            )
        }
    }

    VideoScaffold(
        player = player,
        title = title,
        onNavUp = onDismiss,
        modifier = Modifier.background(backgroundColor).then(modifier),
    )
}
