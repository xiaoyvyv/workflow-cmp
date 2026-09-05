package com.xiaoyv.workflow.model.log

import com.xiaoyv.workflow.Immutable
import com.xiaoyv.workflow.model.spec.ActionErrorKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 可由 UI、日志系统和失败分支共同消费的执行错误。
 */
@Immutable
@Serializable
data class ActionExecutionError(
    val code: String,
    val message: String,
    val nodeId: String? = null,
    val details: JsonObject = JsonObject(emptyMap()),
) {
    /**
     * 生成适合控制台打印或开发日志记录的高可读性格式化日志块，与 [ActionWorkflowException.buildFormattedTraceLog] 格式一致。
     */
    fun buildFormattedTraceLog(): String {
        val workflowId = (details[ActionErrorKey.WORKFLOW_ID] as? JsonPrimitive)?.contentOrNull
        val workflowName = (details[ActionErrorKey.WORKFLOW_NAME] as? JsonPrimitive)?.contentOrNull
        val nodeType = (details[ActionErrorKey.NODE_TYPE] as? JsonPrimitive)?.contentOrNull
        val nodeLabel = (details[ActionErrorKey.NODE_LABEL] as? JsonPrimitive)?.contentOrNull
        val configKey = (details[ActionErrorKey.CONFIG_KEY] as? JsonPrimitive)?.contentOrNull
        val hint = (details[ActionErrorKey.HINT] as? JsonPrimitive)?.contentOrNull
        val causeClass = (details[ActionErrorKey.CAUSE_CLASS] as? JsonPrimitive)?.contentOrNull
        val causeMsg = (details[ActionErrorKey.CAUSE_MESSAGE] as? JsonPrimitive)?.contentOrNull

        val specialKeys = setOf(
            ActionErrorKey.WORKFLOW_ID,
            ActionErrorKey.WORKFLOW_NAME,
            ActionErrorKey.NODE_TYPE,
            ActionErrorKey.NODE_LABEL,
            ActionErrorKey.CONFIG_KEY,
            ActionErrorKey.HINT,
            ActionErrorKey.CAUSE_CLASS,
            ActionErrorKey.CAUSE_MESSAGE,
        )
        val contextSnapshot = details.filterKeys { it !in specialKeys }

        return buildString {
            appendLine("================================================================================")
            appendLine("❌ [WORKFLOW ERROR TRACE] 工作流执行异常溯源报告")
            appendLine("--------------------------------------------------------------------------------")
            if (!workflowId.isNullOrEmpty()) {
                val namePart = if (!workflowName.isNullOrEmpty()) " $workflowName" else ""
                appendLine("📍 工作流标识 : [$workflowId]$namePart")
            }
            if (!nodeId.isNullOrEmpty()) {
                appendLine("📍 节点标识   : [$nodeId]")
            }
            if (!nodeType.isNullOrEmpty()) {
                val labelPart = if (!nodeLabel.isNullOrEmpty()) " ($nodeLabel)" else ""
                appendLine("📍 节点类型   : $nodeType$labelPart")
            }
            appendLine("📍 错误代码   : $code")
            appendLine("📍 错误原因   : $message")
            if (!configKey.isNullOrEmpty()) {
                appendLine("📍 问题配置项 : $configKey")
            }
            if (contextSnapshot.isNotEmpty()) {
                appendLine("📍 上下文快照 : ${JsonObject(contextSnapshot)}")
            }
            if (!causeClass.isNullOrEmpty() || !causeMsg.isNullOrEmpty()) {
                val causeFormatted = listOfNotNull(causeClass?.takeIf { it.isNotEmpty() }, causeMsg?.takeIf { it.isNotEmpty() }).joinToString(": ")
                appendLine("📍 底层异常   : $causeFormatted")
            }
            if (!hint.isNullOrEmpty()) {
                appendLine("💡 排查建议   : $hint")
            }
            appendLine("================================================================================")
        }
    }
}
