package com.xiaoyv.workflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.xiaoyv.workflow.demo.WorkflowsRoute
import com.xiaoyv.workflow.demo.WorkflowsViewModel
import com.xiaoyv.workflow.demo.rememberWorkflowsViewModel
import com.xiaoyv.workflow.demo.support.WorkflowEditorBridgePanel

/**
 * 应用全平台入口，展示工作流交互示例页面及 Web 编辑器服务悬浮面板。
 */
@Composable
@Preview
fun App(viewModel: WorkflowsViewModel = rememberWorkflowsViewModel()) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            WorkflowsRoute(viewModel)
            WorkflowEditorBridgePanel(
                viewModel = viewModel,
            )
        }
    }
}
