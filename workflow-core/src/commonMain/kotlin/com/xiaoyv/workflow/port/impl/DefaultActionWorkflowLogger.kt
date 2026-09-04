package com.xiaoyv.workflow.port.impl

import com.xiaoyv.workflow.port.ActionWorkflowLogger
import kotlinx.serialization.json.JsonElement

/**
 * 默认将工作流日志输出到控制台的实现。
 */
class DefaultActionWorkflowLogger : ActionWorkflowLogger {
    override fun log(level: String, message: String, data: JsonElement?) {
        val detail = if (data != null) " | data=$data" else ""
        println("[ActionWorkflow][$level] $message$detail")
    }
}