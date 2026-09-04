package com.xiaoyv.workflow.ui.core.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ContentMargins(
    val contentMargin: Dp,
    val contentMarginHalf: Dp,
)

val LocalWorkflowUiLayout = compositionLocalOf {
    ContentMargins(
        contentMargin = 16.dp,
        contentMarginHalf = 8.dp,
    )
}

val WorkflowContentMargin: Dp
    @Composable @ReadOnlyComposable
    get() = LocalWorkflowUiLayout.current.contentMargin

val WorkflowContentMarginHalf: Dp
    @Composable @ReadOnlyComposable
    get() = LocalWorkflowUiLayout.current.contentMarginHalf

val WorkflowContentMarginGrid: Dp
    @Composable @ReadOnlyComposable
    get() = LocalWorkflowUiLayout.current.contentMarginHalf + LocalWorkflowUiLayout.current.contentMarginHalf / 2
