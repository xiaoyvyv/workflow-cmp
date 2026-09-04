package com.xiaoyv.workflow.ui.image

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.xiaoyv.workflow.ui.platform.back.PlatformBackHandler
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 漫画图片预览阅读模式。
 */
enum class WorkflowImagePreviewMode(val label: String) {
    /**
     * 水平单页/双页翻页模式。
     */
    HORIZONTAL("水平翻页"),

    /**
     * 竖向无缝条漫连续滚动模式。
     */
    VERTICAL("竖直条漫"),
}

/**
 * 专业漫画软件体验的图片预览页面。
 *
 * 支持水平分页（HorizontalPager）与竖直条漫（LazyColumn，上下3f倍预渲染）两种阅读模式；
 * 提供沉浸式手势触控、状态栏/导航栏遮罩、进度滑块、前后页跳转及无缝模式切换。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowImagePreviewPage(
    images: List<String>,
    initialIndex: Int = 0,
    initialMode: WorkflowImagePreviewMode = WorkflowImagePreviewMode.HORIZONTAL,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty()) return

    println(images.joinToString(","))

    PlatformBackHandler(enabled = true) {
        onDismiss()
    }

    val coroutineScope = rememberCoroutineScope()
    val safeInitialIndex = initialIndex.coerceIn(0, images.lastIndex)

    var readingMode by remember { mutableStateOf(initialMode) }
    var showControls by remember { mutableStateOf(true) }

    // 水平模式状态
    val pagerState = rememberPagerState(
        initialPage = safeInitialIndex,
        pageCount = { images.size },
    )

    // 竖直条漫模式状态（配置预渲染上下 3f 倍）
    val verticalListState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(aheadFraction = 3f, behindFraction = 3f),
        initialFirstVisibleItemIndex = safeInitialIndex,
    )

    // 当前实际显示的页码（0-indexed）
    val currentPage by remember {
        derivedStateOf {
            when (readingMode) {
                WorkflowImagePreviewMode.HORIZONTAL -> pagerState.currentPage
                WorkflowImagePreviewMode.VERTICAL -> verticalListState.firstVisibleItemIndex
            }.coerceIn(0, images.lastIndex)
        }
    }

    // 底部滑块拖动状态，拖动过程中仅改变本地预览值，松手后再平滑跳转
    var sliderDragPosition by remember { mutableStateOf<Float?>(null) }

    // 页面跳转逻辑
    val jumpToPage: (Int) -> Unit = { targetIndex ->
        val clamped = targetIndex.coerceIn(0, images.lastIndex)
        coroutineScope.launch {
            when (readingMode) {
                WorkflowImagePreviewMode.HORIZONTAL -> pagerState.animateScrollToPage(clamped)
                WorkflowImagePreviewMode.VERTICAL -> verticalListState.animateScrollToItem(clamped)
            }
        }
    }

    // 切换阅读模式逻辑（同步当前浏览页码）
    val switchMode: (WorkflowImagePreviewMode) -> Unit = { newMode ->
        if (readingMode != newMode) {
            val targetIndex = currentPage
            readingMode = newMode
            coroutineScope.launch {
                when (newMode) {
                    WorkflowImagePreviewMode.HORIZONTAL -> pagerState.scrollToPage(targetIndex)
                    WorkflowImagePreviewMode.VERTICAL -> verticalListState.scrollToItem(targetIndex)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0E)),
    ) {
        // 主阅读内容区域
        when (readingMode) {
            WorkflowImagePreviewMode.HORIZONTAL -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    HorizontalMangaPage(
                        url = images[page],
                        pageIndex = page,
                        totalPages = images.size,
                        onTap = { showControls = !showControls },
                    )
                }
            }

            WorkflowImagePreviewMode.VERTICAL -> {
                VerticalMangaReader(
                    images = images,
                    lazyListState = verticalListState,
                    onTap = { showControls = !showControls },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // 顶部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            MangaTopBar(
                currentPage = currentPage,
                totalPages = images.size,
                currentMode = readingMode,
                onBack = onDismiss,
                onToggleMode = {
                    val next = if (readingMode == WorkflowImagePreviewMode.HORIZONTAL) {
                        WorkflowImagePreviewMode.VERTICAL
                    } else {
                        WorkflowImagePreviewMode.HORIZONTAL
                    }
                    switchMode(next)
                },
            )
        }

        // 底部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            MangaBottomBar(
                currentPage = currentPage,
                totalPages = images.size,
                currentMode = readingMode,
                sliderDragPosition = sliderDragPosition,
                onSliderChange = { sliderDragPosition = it },
                onSliderChangeFinished = {
                    val target = (sliderDragPosition ?: currentPage.toFloat()).roundToInt()
                    sliderDragPosition = null
                    jumpToPage(target)
                },
                onPrevPage = { jumpToPage(currentPage - 1) },
                onNextPage = { jumpToPage(currentPage + 1) },
                onSelectMode = switchMode,
            )
        }

        // 全屏沉浸式阅读时的浮动微型页码指示器
        AnimatedVisibility(
            visible = !showControls && images.size > 1,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, end = 16.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 6.dp,
            ) {
                Text(
                    text = "${currentPage + 1} / ${images.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/**
 * 水平分页漫画页面（基于 CoilZoomAsyncImage，支持高倍手势缩放与拖拽）。
 */
@Composable
private fun HorizontalMangaPage(
    url: String,
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

/**
 * 竖向条漫阅读组件（使用 LazyColumn 且已配置预渲染上下 3f 倍，提供双击缩放与拖拽支持）。
 */
@Composable
private fun VerticalMangaReader(
    images: List<String>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
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
                } else Modifier
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
                VerticalMangaItem(
                    url = images[index],
                    pageIndex = index,
                    totalPages = images.size,
                    onTap = onTap,
                )
            }
        }
    }
}

/**
 * 竖向条漫单张图片项。
 */
@Composable
private fun VerticalMangaItem(
    url: String,
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
 * 漫画顶部沉浸式工具栏。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaTopBar(
    currentPage: Int,
    totalPages: Int,
    currentMode: WorkflowImagePreviewMode,
    onBack: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.4f),
                        Color.Transparent,
                    ),
                ),
            )
            .statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                )
            }
        },
        title = {
            Column {
                Text(
                    text = "第 ${currentPage + 1} / $totalPages 页",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = currentMode.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleMode) {
                Icon(
                    imageVector = when (currentMode) {
                        WorkflowImagePreviewMode.HORIZONTAL -> Icons.Default.ViewStream
                        WorkflowImagePreviewMode.VERTICAL -> Icons.Default.ViewCarousel
                    },
                    contentDescription = when (currentMode) {
                        WorkflowImagePreviewMode.HORIZONTAL -> "切换为竖直条漫"
                        WorkflowImagePreviewMode.VERTICAL -> "切换为水平翻页"
                    },
                )
            }
        },
    )
}

/**
 * 漫画底部专业控制栏。
 */
@Composable
private fun MangaBottomBar(
    currentPage: Int,
    totalPages: Int,
    currentMode: WorkflowImagePreviewMode,
    sliderDragPosition: Float?,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onSelectMode: (WorkflowImagePreviewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayPage = (sliderDragPosition?.roundToInt() ?: currentPage).coerceIn(0, totalPages - 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.6f),
                        Color.Black.copy(alpha = 0.92f),
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 第一行：页面进度跳转滑块与快速翻页按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onPrevPage,
                enabled = currentPage > 0,
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一页",
                    tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.25f),
                )
            }

            Text(
                text = "${displayPage + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
            )

            Slider(
                value = (sliderDragPosition ?: currentPage.toFloat()).coerceIn(0f, (totalPages - 1).coerceAtLeast(0).toFloat()),
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderChangeFinished,
                valueRange = 0f..(totalPages - 1).coerceAtLeast(0).toFloat(),
                steps = if (totalPages > 2) totalPages - 2 else 0,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                ),
            )

            Text(
                text = "$totalPages",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.width(36.dp),
            )

            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1,
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一页",
                    tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.25f),
                )
            }
        }

        // 第二行：阅读模式切换器（胶囊按钮）
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                WorkflowImagePreviewMode.entries.forEach { mode ->
                    val isSelected = currentMode == mode
                    val icon = when (mode) {
                        WorkflowImagePreviewMode.HORIZONTAL -> Icons.Default.ViewCarousel
                        WorkflowImagePreviewMode.VERTICAL -> Icons.Default.ViewStream
                    }

                    Surface(
                        onClick = { onSelectMode(mode) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.75f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
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
