package com.xiaoyv.workflow.model.execution

import com.xiaoyv.workflow.Immutable
import com.xiaoyv.workflow.util.serialization.SerializeMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 工作流节点执行后的出口和结构化输出。
 */
@Immutable
data class ActionNodeExecutionResult(
    val outputPortId: String,
    val output: JsonObject = JsonObject(emptyMap()),
    val variableUpdates: SerializeMap<String, JsonElement> = persistentMapOf(),
    val sideEffect: ActionSideEffect? = null,
)

/**
 * 由宿主 ViewModel/平台层执行的副作用。
 */
@Immutable
interface ActionSideEffect
