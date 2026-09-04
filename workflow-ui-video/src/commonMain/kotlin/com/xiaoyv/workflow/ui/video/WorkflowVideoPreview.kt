package com.xiaoyv.workflow.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.openani.mediamp.compose.rememberMediampPlayer
import org.openani.mediamp.playUri

/**
 * 对应 PreviewVideo 的默认 MediaMP 播放页面。
 */
@Composable
fun WorkflowVideoPreviewPage(
    videoUrl: String,
    title: String = "视频预览",
    onNavUp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val player = rememberMediampPlayer()

    LaunchedEffect(key1 = videoUrl) {
        if (videoUrl.isNotBlank()) {
            player.playUri(uri = videoUrl)
        }
    }

    VideoScaffold(
        player = player,
        title = title,
        onNavUp = onNavUp,
        modifier = modifier,
    )
}
