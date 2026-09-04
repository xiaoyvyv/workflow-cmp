package com.xiaoyv.workflow.ui.image

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.xiaoyv.workflow.ui.image.internal.ImagePreviewBottomBar
import com.xiaoyv.workflow.ui.image.internal.ImagePreviewHorizontalPage
import com.xiaoyv.workflow.ui.image.internal.ImagePreviewPageIndicator
import com.xiaoyv.workflow.ui.image.internal.ImagePreviewTopBar
import com.xiaoyv.workflow.ui.image.internal.ImagePreviewVerticalReader
import com.xiaoyv.workflow.ui.platform.back.PlatformBackHandler
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 图片预览阅读模式。
 */
enum class WorkflowImagePreviewMode(val label: String) {
    /**
     * 水平单页翻页模式。
     */
    HORIZONTAL("水平翻页"),

    /**
     * 竖向无缝条漫连续滚动模式。
     */
    VERTICAL("竖直条漫"),
}

/**
 * 图片预览页面。
 *
 * 支持水平分页（HorizontalPager）与竖直条漫（LazyColumn，上下 3f 倍预渲染）两种阅读模式；
 * 提供沉浸式手势触控、状态栏/导航栏遮罩、进度滑块、前后页跳转及无缝模式切换。
 *
 * 内容渲染区域（[ImagePreviewHorizontalPage] / [ImagePreviewVerticalReader]）各自独立，
 * 互不耦合，通过外层统一的控制层进行模式调度。
 */
@Composable
fun WorkflowImagePreviewPage(
    images: List<ImageRequest>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    initialMode: WorkflowImagePreviewMode = WorkflowImagePreviewMode.HORIZONTAL,
    captureBackPressed: Boolean = true
) {
    if (images.isEmpty()) return

    PlatformBackHandler(enabled = captureBackPressed) { onDismiss() }

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

    // 页面跳转逻辑（带动画：用于按钮点击；无动画：用于滑块拖拽实时同步）
    val jumpToPage: (Int) -> Unit = { targetIndex ->
        val clamped = targetIndex.coerceIn(0, images.lastIndex)
        coroutineScope.launch {
            when (readingMode) {
                WorkflowImagePreviewMode.HORIZONTAL -> pagerState.animateScrollToPage(clamped)
                WorkflowImagePreviewMode.VERTICAL -> verticalListState.animateScrollToItem(clamped)
            }
        }
    }

    val jumpToPageImmediate: (Int) -> Unit = { targetIndex ->
        val clamped = targetIndex.coerceIn(0, images.lastIndex)
        coroutineScope.launch {
            when (readingMode) {
                WorkflowImagePreviewMode.HORIZONTAL -> pagerState.scrollToPage(clamped)
                WorkflowImagePreviewMode.VERTICAL -> verticalListState.scrollToItem(clamped)
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
        // 内容渲染区域：水平与竖向各自独立，互不耦合
        when (readingMode) {
            WorkflowImagePreviewMode.HORIZONTAL -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    ImagePreviewHorizontalPage(
                        url = images[page],
                        pageIndex = page,
                        totalPages = images.size,
                        onTap = { showControls = !showControls },
                    )
                }
            }

            WorkflowImagePreviewMode.VERTICAL -> {
                ImagePreviewVerticalReader(
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
            ImagePreviewTopBar(
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
            ImagePreviewBottomBar(
                currentPage = currentPage,
                totalPages = images.size,
                sliderDragPosition = sliderDragPosition,
                onSliderChange = { pos ->
                    sliderDragPosition = pos
                    // 拖拽过程中实时无动画同步页面
                    jumpToPageImmediate(pos.roundToInt())
                },
                onSliderChangeFinished = {
                    // 松手时清除拖拽预览值即可，页面已在拖拽中同步
                    sliderDragPosition = null
                },
                onPrevPage = { jumpToPage(currentPage - 1) },
                onNextPage = { jumpToPage(currentPage + 1) },
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
            ImagePreviewPageIndicator(
                currentPage = currentPage,
                totalPages = images.size,
            )
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
