package com.xiaoyv.workflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.xiaoyv.workflow.demo.WorkflowsRoute

/**
 * 应用默认入口，直接展示工作流交互示例页面。
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        WorkflowsRoute()
    }
}
