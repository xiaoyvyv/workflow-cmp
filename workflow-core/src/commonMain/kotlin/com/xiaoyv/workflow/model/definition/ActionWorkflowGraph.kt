package com.xiaoyv.workflow.model.definition

import com.xiaoyv.workflow.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 工作流中的单个逻辑处理节点。
 */
@Immutable
@Serializable
data class ActionNode(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("nodeVersion")
    val nodeVersion: Int = 1,
    @SerialName("label")
    val label: String = "",
    @SerialName("config")
    val config: JsonObject = JsonObject(emptyMap()),
    @SerialName("layout")
    val layout: ActionNodeLayout = ActionNodeLayout(),
)

/**
 * 可选的节点 UI 可视化画布坐标。
 */
@Immutable
@Serializable
data class ActionNodeLayout(
    @SerialName("x")
    val x: Float = 0f,
    @SerialName("y")
    val y: Float = 0f,
)

/**
 * 连接源节点输出端口与目标节点输入端口的控制边。
 */
@Immutable
@Serializable
data class ActionEdge(
    @SerialName("id")
    val id: String,
    @SerialName("kind")
    val kind: String = ActionPortKind.CONTROL,
    @SerialName("source")
    val source: ActionPortRef,
    @SerialName("target")
    val target: ActionPortRef,
)

/**
 * 节点的端口引用，指定具体的节点 ID 与端口 ID。
 */
@Immutable
@Serializable
data class ActionPortRef(
    @SerialName("nodeId")
    val nodeId: String,
    @SerialName("portId")
    val portId: String,
)

/**
 * 端口连线类型。
 */
object ActionPortKind {
    const val CONTROL = "control"
    const val DATA = "data"
}
