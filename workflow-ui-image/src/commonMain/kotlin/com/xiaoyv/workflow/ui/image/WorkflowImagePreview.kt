package com.xiaoyv.workflow.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.github.panpf.zoomimage.CoilZoomAsyncImage

/**
 * 对应 PreviewMain 的默认缩放图片预览页。
 */
@Composable
fun WorkflowImagePreviewPage(
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty()) return
    val pager = rememberPagerState(initialPage = initialIndex.coerceIn(images.indices)) {
        images.size
    }
    HorizontalPager(modifier = modifier.fillMaxSize().background(Color.Black), state = pager) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            var loading by remember(page) {
                mutableStateOf(true)
            }
            CoilZoomAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = images[page],
                contentDescription = "图片 ${page + 1}",
                onState = {
                    loading = it is AsyncImagePainter.State.Loading
                },
                onTap = {
                    onDismiss()
                },
            )
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

/**
 * 工作流选择对话框使用的远程图片缩略图。
 */
@Composable
fun WorkflowSelectOptionImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}
