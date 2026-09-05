package com.xiaoyv.workflow.model.definition

import com.xiaoyv.workflow.Immutable
import com.xiaoyv.workflow.util.serialization.SerializeList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 代表一条完整、可序列化、可在客户端引擎中运行的声明式工作流。
 */
@Immutable
@Serializable
data class ActionWorkflow(
    @SerialName("formatVersion")
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("enabled")
    val enabled: Boolean = true,
    @SerialName("entryNodeId")
    val entryNodeId: String = "",
    @SerialName("globalErrorNodeId")
    val globalErrorNodeId: String? = null,
    @SerialName("requiredCapabilities")
    val requiredCapabilities: SerializeList<String> = persistentListOf(),
    @SerialName("nodes")
    val nodes: SerializeList<ActionNode> = persistentListOf(),
    @SerialName("edges")
    val edges: SerializeList<ActionEdge> = persistentListOf(),
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 1
    }
}
