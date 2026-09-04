package com.xiaoyv.workflow.ui.image.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest

/**
 * 竖向条漫阅读组件（使用 LazyColumn 且已配置预渲染上下 3f 倍，提供双击缩放与拖拽支持）。
 */
@Composable
internal fun ImagePreviewVerticalReader(
    images: List<ImageRequest>,
    lazyListState: LazyListState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            }
            .then(
                if (scale > 1.05f) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale <= 1.05f) {
                                offset = Offset.Zero
                            } else {
                                offset += pan
                            }
                        }
                    }
                } else Modifier,
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(
                count = images.size,
                key = { index -> "${images[index]}_$index" },
            ) { index ->
                ImagePreviewVerticalItem(
                    url = images[index],
                    pageIndex = index,
                    totalPages = images.size,
                    onTap = onTap,
                )
            }

            // 末尾占位：保证可以滑动到底，同时给出阅读完毕提示
            item(key = "__end_placeholder__") {
                ImagePreviewEndPlaceholder()
            }
        }
    }
}

/**
 * 竖向条漫单张图片项。
 */
@Composable
internal fun ImagePreviewVerticalItem(
    url: ImageRequest,
    pageIndex: Int,
    totalPages: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var loading by remember(url) { mutableStateOf(true) }
    var isError by remember(url) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = "第 ${pageIndex + 1} / $totalPages 页",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth,
            onState = { state ->
                loading = state is AsyncImagePainter.State.Loading
                isError = state is AsyncImagePainter.State.Error
                if (state is AsyncImagePainter.State.Error) {
                    println("WorkflowImagePreview: Error loading vertical page ${pageIndex + 1} ($url): ${state.result.throwable}")
                    state.result.throwable.printStackTrace()
                }
            },
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF141416)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "正在加载第 ${pageIndex + 1} 页...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        } else if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF18181B)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp),
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
}

/**
 * 竖向条漫末尾占位组件。
 *
 * 高度撑满整个可见区域（fillParentMaxSize），使末尾页如同独立一页，
 * 保证用户可以完整滑动到底，内容居中展示阅读完毕提示。
 */
@Composable
internal fun LazyItemScope.ImagePreviewEndPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillParentMaxSize()
            .background(Color(0xFF0D0D0E)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp),
            )

            Text(
                text = "已到达最后一页",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal,
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.05f),
            ) {
                Text(
                    text = "向上滑动返回顶部",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}
