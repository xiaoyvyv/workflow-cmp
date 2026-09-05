package com.xiaoyv.workflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.xiaoyv.workflow.demo.WorkflowsRoute
import com.xiaoyv.workflow.demo.WorkflowsViewModel
import com.xiaoyv.workflow.demo.rememberWorkflowsViewModel

/**
 * 应用默认入口，直接展示工作流交互示例页面。
 */
@Composable
@Preview
fun App(viewModel: WorkflowsViewModel = rememberWorkflowsViewModel()) {
    MaterialTheme {
        WorkflowsRoute(viewModel)
    }
}
