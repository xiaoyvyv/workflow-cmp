package com.xiaoyv.workflow.ui.image.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.xiaoyv.workflow.ui.image.WorkflowImagePreviewMode

/**
 * 图片预览顶部沉浸式工具栏。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImagePreviewTopBar(
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
