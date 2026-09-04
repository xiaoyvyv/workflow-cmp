package com.xiaoyv.workflow

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.xiaoyv.workflow.demo.WorkflowsRoute
import com.xiaoyv.workflow.demo.createWorkflowsViewModel
import com.xiaoyv.workflow.demo.repository.InMemoryActionWorkflowRepository
import com.xiaoyv.workflow.di.createWorkflowRuntime
import com.xiaoyv.workflow.port.impl.DefaultActionHttpRequestExecutor
import io.ktor.client.HttpClient

/**
 * 应用默认入口，直接展示工作流交互示例页面。
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        val httpClient = remember {
            HttpClient { }
        }

        val runtime = remember {
            createWorkflowRuntime(
                httpRequestExecutor = DefaultActionHttpRequestExecutor(httpClient)
            )
        }
        val viewModel = remember(runtime) {
            createWorkflowsViewModel(
                repository = InMemoryActionWorkflowRepository(
                    validator = runtime.validator,
                    codec = runtime.codec,
                ),
                engine = runtime.engine,
            )
        }
        WorkflowsRoute(
            viewModel = viewModel,
            onNavUp = {},
            onNavScreen = {},
            nodeRegistry = runtime.registry,
        )
    }
}
