package com.xiaoyv.workflow.demo.support

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val ContentMargin = 16.dp
val ContentMarginHalf = 8.dp

object Res {
    object string {
        const val workflow_title = "工作流"
        const val workflow_example_run = "运行"
        const val workflow_example_copy = "复制"
        const val workflow_example_copied = "工作流已复制到剪贴板"
        const val workflow_run_title = "运行结果"
        const val workflow_view_node_graph = "节点图"
        const val workflow_view_execution_log = "执行日志"
        const val workflow_run_empty_log = "尚未运行工作流"
        const val workflow_run_running = "运行中"
        const val workflow_run_finished = "已完成"
        const val workflow_run_error = "运行失败"
        const val workflow_run_nodes = "执行节点：%1\$s"
        const val workflow_run_opened_url = "打开链接：%1\$s"
        const val workflow_run_output = "执行输出"
    }
}

fun stringResource(value: String, vararg args: Any): String {
    if (args.isEmpty()) return value
    var result = value
    for ((index, arg) in args.withIndex()) {
        val indexedStr = "%${index + 1}\$s"
        val indexedInt = "%${index + 1}\$d"
        when {
            result.contains(indexedStr) -> result = result.replace(indexedStr, arg.toString())
            result.contains(indexedInt) -> result = result.replace(indexedInt, arg.toString())
            result.contains("%s") -> result = result.replaceFirst("%s", arg.toString())
            result.contains("%d") -> result = result.replaceFirst("%d", arg.toString())
            else -> result = "$result $arg"
        }
    }
    return result
}

@Composable
fun BgmTopAppBar(title: String, onNavigationClick: () -> Unit = {}) {
    CenterAlignedTopAppBar(title = {
        Text(title)
    })
}

@Composable
fun PreviewColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxSize().padding(ContentMargin), content = {
        content()
    })
}
