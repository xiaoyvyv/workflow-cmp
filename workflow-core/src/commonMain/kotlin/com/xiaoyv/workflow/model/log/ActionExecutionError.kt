package com.xiaoyv.workflow.model.log

import com.xiaoyv.workflow.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
)
