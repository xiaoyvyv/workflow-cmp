package com.xiaoyv.workflow.codec

import com.xiaoyv.workflow.model.spec.ActionCapability
import com.xiaoyv.workflow.model.spec.ActionCapabilitySpec
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.node.core.ActionPortSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 将当前注册表导出为任意编辑器可消费的、纯描述性的节点目录。
 * 该 codec 不导出执行器、宿主能力实现或机密数据。
 * 设备端仍必须使用自身注册表校验工作流。
 */
class ActionEditorManifestCodec(
    private val json: Json,
    private val registry: ActionNodeRegistry,
) {
    fun export(): String {
        registry.validateEditorSpecs().requireValid()
        return json.encodeToString(ActionEditorManifest.serializer(), registry.toEditorManifest())
    }
}

/**
 * 可在构建期与测试期执行的漂移检查，用于比对执行需求和编辑器可见配置。
 */
data class ActionEditorSpecValidation(val issues: List<String>) {
    val isValid: Boolean
        get() = issues.isEmpty()

    fun requireValid() = require(isValid) { issues.joinToString("\n") }
}

fun ActionNodeRegistry.validateEditorSpecs(): ActionEditorSpecValidation {
    val issues =
        all().flatMap { definition ->
            val spec = definition.spec
            val editor = spec.editor
            val declared = editor.fields.filterNot { it.editorHidden }.map { it.key }.toSet()
            val hidden = editor.fields.filter { it.editorHidden }.map { it.key }.toSet()
            buildList {
                spec.requiredConfigKeys
                    .filter { it !in declared && it !in hidden }
                    .forEach {
                        add("${spec.type}: required config '$it' is absent from editor fields")
                    }
                editor.defaultConfig.keys
                    .filter { it !in declared && it !in hidden }
                    .forEach {
                        add("${spec.type}: default config '$it' has no editor field")
                    }
                editor.fields
                    .filter {
                        it.sensitive && (it.defaultValue != null || it.key in editor.defaultConfig)
                    }
                    .forEach {
                        add(
                            "${spec.type}: sensitive field '${it.key}' must not declare a default value"
                        )
                    }
            }
        }
    return ActionEditorSpecValidation(issues)
}

/**
 * 当前注册表的编辑器 Manifest。
 */
@Serializable
data class ActionEditorManifest(
    val schemaVersion: Int = ActionEditorSchema.VERSION,
    val workflowFormatVersion: Int =
        com.xiaoyv.workflow.model.definition.ActionWorkflow.CURRENT_FORMAT_VERSION,
    val nodeTypes: List<ActionEditorNodeManifest>,
    val categories: List<ActionEditorCategoryManifest>,
    val capabilities: List<ActionCapabilitySpec> = emptyList(),
)

/**
 * 独立 Manifest 与离线编辑器打包封套共享的版本号。
 */
object ActionEditorSchema {
    const val VERSION = 1
}

/**
 * 单个节点的完整编辑器描述。
 */
@Serializable
data class ActionEditorNodeManifest(
    val type: String,
    val latestVersion: Int,
    val category: String,
    val inputPorts: List<ActionPortSpec>,
    val outputPorts: List<ActionPortSpec>,
    val requiredConfigKeys: Set<String>,
    val requiredCapabilities: Set<String>,
    val editor: ActionNodeEditorSpec,
)

/**
 * 由已注册节点规格生成的稳定面板分组。
 */
@Serializable
data class ActionEditorCategoryManifest(
    val id: String,
    val label: String = id,
    val nodeTypes: List<String>,
)

/**
 * 由注册表生成 Manifest；所有节点均携带非空的编辑器描述。
 */
fun ActionNodeRegistry.toEditorManifest(): ActionEditorManifest =
    ActionEditorManifest(
        nodeTypes =
            all()
                .asSequence()
                .map { it.spec }
                .sortedWith(
                    compareBy<ActionNodeSpec> { com.xiaoyv.workflow.node.core.ActionNodeCategory.orderOf(it.category) }
                        .thenBy { it.category }
                        .thenBy { it.type }
                )
                .map(ActionNodeSpec::toEditorManifest)
                .toList(),
        categories =
            all()
                .asSequence()
                .map { it.spec }
                .groupBy { it.category }
                .entries
                .sortedWith(
                    compareBy<Map.Entry<String, List<ActionNodeSpec>>> {
                        com.xiaoyv.workflow.node.core.ActionNodeCategory.orderOf(it.key)
                    }.thenBy { it.key }
                )
                .map { (category, specs) ->
                    val catSpec = com.xiaoyv.workflow.node.core.ActionNodeCategory.specOf(category)
                    ActionEditorCategoryManifest(
                        id = category,
                        label = catSpec.label,
                        nodeTypes = specs.map { it.type }.sorted(),
                    )
                },
        capabilities = buildList {
            val registeredCapabilityIds = all().flatMap { it.spec.requiredCapabilities }.toSet()
            val allCapabilityIds = (ActionCapability.allSpecs.keys + registeredCapabilityIds).sorted()
            allCapabilityIds.forEach { id ->
                add(ActionCapability.specOf(id))
            }
        },
    )

private fun ActionNodeSpec.toEditorManifest() =
    ActionEditorNodeManifest(
        type = type,
        latestVersion = latestVersion,
        category = category,
        inputPorts = inputPorts.sortedBy(ActionPortSpec::order),
        outputPorts = outputPorts.sortedBy(ActionPortSpec::order),
        requiredConfigKeys = requiredConfigKeys,
        requiredCapabilities = requiredCapabilities,
        editor = editor,
    )

/**
 * 离线编辑器可导入的 Manifest 与工作流打包格式。
 */
@Serializable
data class ActionEditorBundle(
    val schemaVersion: Int = ActionEditorSchema.VERSION,
    val manifest: ActionEditorManifest,
    val workflow: com.xiaoyv.workflow.model.definition.ActionWorkflow,
)

/**
 * 仅负责离线打包；工作流导入、迁移与校验仍由 [ActionWorkflowCodec] 负责。
 */
class ActionEditorBundleCodec(private val json: Json) {
    fun export(bundle: ActionEditorBundle): String =
        json.encodeToString(ActionEditorBundle.serializer(), bundle)

    fun import(raw: String): ActionEditorBundle =
        json.decodeFromString(ActionEditorBundle.serializer(), raw)
}

fun <K : Comparable<K>, V> Map<K, V>.toSortedMap(): Map<K, V> {
    return this.entries
        .sortedBy { it.key }
        .associate { it.key to it.value }
}
