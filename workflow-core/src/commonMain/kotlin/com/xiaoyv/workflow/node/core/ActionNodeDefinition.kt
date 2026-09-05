package com.xiaoyv.workflow.node.core

import com.xiaoyv.workflow.Immutable
import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionPortKind
import com.xiaoyv.workflow.model.execution.ActionExecutionContext
import com.xiaoyv.workflow.model.execution.ActionNodeExecutionResult
import com.xiaoyv.workflow.node.core.ActionPortConnectionLimit.UNLIMITED
import com.xiaoyv.workflow.util.serialization.SerializeList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 节点的运行时定义，由规格、执行器与可选迁移器组成。
 */
@Immutable
data class ActionNodeDefinition(
    val spec: ActionNodeSpec,
    val executor: ActionNodeExecutor,
    val migrator: ActionNodeMigrator = ActionNodeMigrator.None,
    val capabilityResolver: ActionNodeCapabilityResolver = ActionNodeCapabilityResolver.FromSpec,
)

/**
 * 根据节点实际配置计算执行所需能力。
 *
 * 某些能力仅在特定控制项启用时需要，例如 HTTP 节点选择携带本地 Cookie；此接口避免把
 * 条件权限硬编码在校验器或引擎中。
 */
fun interface ActionNodeCapabilityResolver {
    /**
     * 返回该节点在当前配置下实际需要的全部能力。
     *
     * @param node 工作流中的节点实例。
     * @param spec 节点静态规格。
     */
    fun requiredCapabilities(node: ActionNode, spec: ActionNodeSpec): Set<String>

    companion object {
        /**
         * 使用 [ActionNodeSpec.requiredCapabilities] 作为节点能力集合的默认实现。
         */
        val FromSpec = ActionNodeCapabilityResolver { _, spec -> spec.requiredCapabilities }
    }
}

/**
 * 供编辑器、校验器和执行器共用的节点端口与配置契约。
 */
@Immutable
@Serializable
data class ActionNodeSpec(
    @SerialName("type")
    val type: String,
    @SerialName("latestVersion")
    val latestVersion: Int = 1,
    @SerialName("category")
    val category: String,
    @SerialName("inputPorts")
    val inputPorts: SerializeList<ActionPortSpec> = persistentListOf(),
    @SerialName("outputPorts")
    val outputPorts: SerializeList<ActionPortSpec> = persistentListOf(),
    @SerialName("requiredConfigKeys")
    val requiredConfigKeys: Set<String> = emptySet(),
    @SerialName("requiredCapabilities")
    val requiredCapabilities: Set<String> = emptySet(),
    // 与渲染技术无关的编辑器描述。
    // 节点执行器和编辑器从同一份 Spec 派生，避免维护独立的前端节点字典。
    @SerialName("editor")
    val editor: ActionNodeEditorSpec,
)

/**
 * 节点单个输入或输出端口的连接约束。
 */
@Immutable
@Serializable
data class ActionPortSpec(
    @SerialName("id")
    val id: String,
    @SerialName("kind")
    val kind: String = ActionPortKind.CONTROL,
    @SerialName("direction")
    val direction: String,
    @SerialName("maxConnections")
    val maxConnections: Int = UNLIMITED,
    @SerialName("label")
    val label: String = id,
    @SerialName("order")
    val order: Int = 0,
)

/**
 * 编辑器可消费的节点视觉与配置表单描述，不包含任何 Compose/HTML 类型
 */
@Immutable
@Serializable
data class ActionNodeEditorSpec(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("defaultConfig")
    val defaultConfig: JsonObject = JsonObject(emptyMap()),
    @SerialName("fields")
    val fields: SerializeList<ActionConfigFieldSpec> = persistentListOf(),
    @SerialName("creatable")
    val creatable: Boolean = true,
)

/**
 * 单个节点配置项的通用表单描述。
 */
@Immutable
@Serializable
data class ActionConfigFieldSpec(
    @SerialName("key")
    val key: String,
    @SerialName("label")
    val label: String = key,
    @SerialName("kind")
    val kind: String = ActionEditorFieldKind.TEXT,
    @SerialName("description")
    val description: String = "",
    @SerialName("group")
    val group: String? = null,
    @SerialName("order")
    val order: Int = 0,
    @SerialName("defaultValue")
    val defaultValue: JsonElement? = null,
    @SerialName("placeholder")
    val placeholder: String? = null,
    @SerialName("options")
    val options: SerializeList<ActionEditorFieldOption> = persistentListOf(),
    @SerialName("required")
    val required: Boolean = false,
    @SerialName("readOnly")
    val readOnly: Boolean = false,
    @SerialName("sensitive")
    val sensitive: Boolean = false,
    @SerialName("editorHidden")
    val editorHidden: Boolean = false,
    @SerialName("visibleWhen")
    val visibleWhen: ActionEditorFieldCondition? = null,
    @SerialName("enabledWhen")
    val enabledWhen: ActionEditorFieldCondition? = null,
    @SerialName("validation")
    val validation: ActionEditorFieldValidation? = null,
)

/**
 * Non-executable config predicate; renderers must never evaluate supplied scripts.
 */
@Immutable
@Serializable
data class ActionEditorFieldCondition(
    @SerialName("key")
    val key: String,
    @SerialName("equals")
    val equals: JsonElement,
)

/**
 * Portable UI validation hints. Runtime semantic validation remains in the node executor/validator.
 */
@Immutable
@Serializable
data class ActionEditorFieldValidation(
    @SerialName("minimum")
    val minimum: Double? = null,
    @SerialName("maximum")
    val maximum: Double? = null,
    @SerialName("pattern")
    val pattern: String? = null,
    @SerialName("minLength")
    val minLength: Int? = null,
    @SerialName("maxLength")
    val maxLength: Int? = null,
)

@Immutable
@Serializable
data class ActionEditorFieldOption(
    @SerialName("value")
    val value: JsonElement,
    @SerialName("label")
    val label: String,
)

/**
 * 编辑器字段类型常量；前端以未知类型降级为只读 JSON，而不能执行脚本。
 */
object ActionEditorFieldKind {
    const val TEXT = "text"
    const val TEMPLATE_TEXT = "template-text"
    const val TEXTAREA = "textarea"
    const val NUMBER = "number"
    const val BOOLEAN = "boolean"
    const val SELECT = "select"
    const val JSON = "json"
}

/**
 * 端口连接数量限制。
 *
 * 仅具有明确合流语义的控制节点应使用 [UNLIMITED]；普通数据与控制输入仍应保持单入边，
 * 以免工作流在尚未支持并发调度时出现不确定的执行顺序。
 */
object ActionPortConnectionLimit {
    const val UNLIMITED = Int.MAX_VALUE
}

/**
 * 节点端口方向常量。
 */
object ActionPortDirection {
    const val INPUT = "input"
    const val OUTPUT = "output"
}

/**
 * 节点运行时执行器。
 */
fun interface ActionNodeExecutor {
    /**
     * 执行节点业务逻辑；副作用应通过结果交由宿主执行。
     *
     * @param node 当前节点配置。
     * @param context 当前不可变执行上下文。
     * @return 节点出口、结构化输出及可选副作用。
     */
    suspend fun execute(node: ActionNode, context: ActionExecutionContext): ActionNodeExecutionResult
}

/**
 * 将历史节点配置迁移到节点规格的最新版本。
 */
fun interface ActionNodeMigrator {
    /**
     * 将单次历史节点配置升级到更高版本。
     *
     * @param node 待迁移节点。
     * @return 必须具有更高 [ActionNode.nodeVersion] 的同类型节点。
     */
    fun migrate(node: ActionNode): ActionNode

    companion object {
        val None = ActionNodeMigrator { node ->
            require(node.nodeVersion == 1) { "节点 ${node.type} 缺少从 v${node.nodeVersion} 的迁移" }
            node
        }
    }
}
