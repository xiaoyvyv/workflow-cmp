package com.xiaoyv.workflow.ui.image.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 图片预览底部控制栏（进度滑块 + 前后翻页）。
 *
 * 模式切换入口已移至顶部工具栏右上角。
 * 滑块使用连续模式（steps=0）避免图片过多时刻度密集，通过 roundToInt 保持页面精度。
 * 拖拽过程中实时回调 [onSliderChange]，由外层即时同步页面，无需等待松手。
 */
@Composable
internal fun ImagePreviewBottomBar(
    currentPage: Int,
    totalPages: Int,
    sliderDragPosition: Float?,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // 页面进度跳转滑块与快速翻页按钮
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

            // steps=0：连续滑动，无刻度点，避免图片过多时刻度密集；精度通过 roundToInt 保证
            Slider(
                value = (sliderDragPosition ?: currentPage.toFloat()).coerceIn(
                    0f,
                    (totalPages - 1).coerceAtLeast(0).toFloat(),
                ),
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderChangeFinished,
                valueRange = 0f..(totalPages - 1).coerceAtLeast(0).toFloat(),
                steps = 0,
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
    }
}
