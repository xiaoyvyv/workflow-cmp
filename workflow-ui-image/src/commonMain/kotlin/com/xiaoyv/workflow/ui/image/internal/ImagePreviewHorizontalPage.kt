package com.xiaoyv.workflow.ui.image.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.github.panpf.zoomimage.CoilZoomAsyncImage

/**
 * 水平分页漫画页面（基于 CoilZoomAsyncImage，支持高倍手势缩放与拖拽）。
 */
@Composable
internal fun ImagePreviewHorizontalPage(
    url: ImageRequest,
    pageIndex: Int,
    totalPages: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var loading by remember(url) { mutableStateOf(true) }
    var isError by remember(url) { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CoilZoomAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = url,
            contentDescription = "第 ${pageIndex + 1} / $totalPages 页",
            contentScale = ContentScale.Fit,
            onState = { state ->
                loading = state is AsyncImagePainter.State.Loading
                isError = state is AsyncImagePainter.State.Error
                if (state is AsyncImagePainter.State.Error) {
                    println("WorkflowImagePreview: Error loading horizontal page ${pageIndex + 1} ($url): ${state.result.throwable}")
                    state.result.throwable.printStackTrace()
                }
            },
            onTap = {
                onTap()
            },
        )

        if (loading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${pageIndex + 1} / $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        } else if (isError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = "第 ${pageIndex + 1} 页加载失败",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}
