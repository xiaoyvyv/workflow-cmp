package com.xiaoyv.workflow.demo.support

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiaoyv.workflow.demo.mvi.UiState

val ContentMargin = 16.dp
val ContentMarginHalf = 8.dp

sealed interface Screen {
    data object Workflows : Screen
}

object Res {
    object string {
        const val workflow_title = "工作流"
        const val workflow_tab_all = "全部"
        const val workflow_tab_practice = "练习"
        const val workflow_tab_flow_control = "流程控制"
        const val workflow_tab_data = "数据"
        const val workflow_tab_text_net = "文本与网络"
        const val workflow_tab_ui_side_effect = "界面交互"
        const val workflow_tab_error = "错误测试"
        const val workflow_tab_saved = "已保存"
        const val workflow_saved_title = "已保存工作流"
        const val workflow_example_run = "运行"
        const val workflow_run_title = "运行结果"
        const val workflow_view_node_graph = "节点图"
        const val workflow_view_execution_log = "执行日志"
        const val workflow_run_empty_log = "尚未运行工作流"
        const val workflow_run_running = "运行中"
        const val workflow_run_finished = "已完成"
        const val workflow_run_error = "运行失败"
        const val workflow_run_nodes = "执行节点：%s"
        const val workflow_run_opened_url = "打开链接：%s"
        const val workflow_run_output = "执行输出"
    }
}

fun stringResource(value: String, vararg args: Any): String =
    if (args.isEmpty()) value else "$value ${args.joinToString()}"

@Composable
fun BgmTopAppBar(title: String, onNavigationClick: () -> Unit) {
    CenterAlignedTopAppBar(title = {
        Text(title)
    })
}

@Composable
fun <T> StateLayout(
    uiState: UiState<T>,
    modifier: Modifier = Modifier,
    onRefresh: (Boolean) -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Column(modifier) {
        content(uiState.data)
    }
}

@Composable
fun PreviewColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxSize().padding(ContentMargin), content = {
        content()
    })
}
