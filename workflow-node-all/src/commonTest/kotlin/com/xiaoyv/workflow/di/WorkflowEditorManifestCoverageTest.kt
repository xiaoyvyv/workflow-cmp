package com.xiaoyv.workflow.di

import com.xiaoyv.workflow.codec.toEditorManifest
import com.xiaoyv.workflow.codec.validateEditorSpecs
import com.xiaoyv.workflow.model.spec.ActionCapability
import com.xiaoyv.workflow.node.core.ActionPortDirection
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkflowEditorManifestCoverageTest {

    private fun createTestRuntime() = createWorkflowRuntime(
        WorkflowRuntimeConfig(
            httpRequestExecutor = ActionHttpRequestExecutor { buildJsonObject {} }
        )
    )

    @Test
    fun exportsAnEditorSpecForEveryBuiltInNode() {
        val runtime = createTestRuntime()
        val manifest = runtime.registry.toEditorManifest()
        val registeredTypes = runtime.registry.all().map { it.spec.type }.toSet()
        val manifestTypes = manifest.nodeTypes.map { it.type }.toSet()

        assertTrue(registeredTypes.isNotEmpty())
        assertEquals(registeredTypes, manifestTypes)
        val validation = runtime.registry.validateEditorSpecs()
        assertTrue(validation.isValid, validation.issues.joinToString("\n"))
        assertTrue(
            manifest.nodeTypes.all { node ->
                node.requiredConfigKeys.all { requiredKey ->
                    node.editor.fields.any { field ->
                        field.key == requiredKey
                    }
                }
            }
        )
    }

    @Test
    fun allCapabilitiesAreDocumentedAndConfigured() {
        val runtime = createTestRuntime()
        val allConfiguredSpecs = ActionCapability.allSpecs

        assertTrue(allConfiguredSpecs.isNotEmpty(), "ActionCapability.allSpecs 必须包含已声明的能力规格")

        // 1. 验证所有已配置的能力规格具备合法的 id、中文名称 label 及说明 description
        allConfiguredSpecs.forEach { (id, spec) ->
            assertEquals(id, spec.id, "能力配置的 Key 与 Spec.id 必须一致")
            assertTrue(spec.label.isNotBlank(), "能力 [$id] 必须配置非空的中文 label")
            assertTrue(spec.description.isNotBlank(), "能力 [$id] 必须配置非空的中文 description")
        }

        // 2. 验证所有已注册节点引用的 requiredCapabilities 都必须在 ActionCapability.allSpecs 中有明确定义与说明
        val allNodeRequiredCaps = runtime.registry.all()
            .flatMap { it.spec.requiredCapabilities }
            .toSet()

        val undocumentedCaps = allNodeRequiredCaps.filter { it !in allConfiguredSpecs }
        assertTrue(
            undocumentedCaps.isEmpty(),
            "存在未在 ActionCapability.allSpecs 中定义说明的节点权限能力: $undocumentedCaps"
        )

        // 3. 验证导出的 manifest capabilities 包含所有已配置能力且规格完整
        val manifest = runtime.registry.toEditorManifest()
        assertTrue(manifest.capabilities.isNotEmpty())
        manifest.capabilities.forEach { spec ->
            assertTrue(spec.id.isNotBlank())
            assertTrue(spec.label.isNotBlank())
        }
    }

    @Test
    fun editorSpecsAreCompleteAndAlignedWithActualSpecs() {
        val runtime = createTestRuntime()
        val registeredNodes = runtime.registry.all()

        assertTrue(registeredNodes.isNotEmpty())

        registeredNodes.forEach { definition ->
            val spec = definition.spec
            val editor = spec.editor
            val type = spec.type

            // 1. 节点基本标题和描述检查
            assertTrue(editor.title.isNotBlank(), "节点 [$type] 的 editor.title 不能为空")

            // 2. 字段 key 唯一性及有效性检查
            val fieldKeys = mutableSetOf<String>()
            editor.fields.forEach { field ->
                assertTrue(field.key.isNotBlank(), "节点 [$type] 包含空 key 的字段")
                assertTrue(field.label.isNotBlank(), "节点 [$type] 的字段 [${field.key}] 的 label 不能为空")
                assertFalse(
                    fieldKeys.contains(field.key),
                    "节点 [$type] 存在重复定义的字段 key: [${field.key}]"
                )
                fieldKeys.add(field.key)

                // 检查 options 中的 label
                field.options.forEach { option ->
                    assertTrue(
                        option.label.isNotBlank(),
                        "节点 [$type] 的字段 [${field.key}] 的选项 label 不能为空"
                    )
                }

                // 检查 visibleWhen / enabledWhen 引用的 key 必须存在于本节点的字段集合中
                field.visibleWhen?.let { cond ->
                    assertTrue(
                        cond.key in fieldKeys || editor.fields.any { it.key == cond.key },
                        "节点 [$type] 的字段 [${field.key}] visibleWhen 引用的 key [${cond.key}] 不在当前节点的 fields 中"
                    )
                }
                field.enabledWhen?.let { cond ->
                    assertTrue(
                        cond.key in fieldKeys || editor.fields.any { it.key == cond.key },
                        "节点 [$type] 的字段 [${field.key}] enabledWhen 引用的 key [${cond.key}] 不在当前节点的 fields 中"
                    )
                }
            }

            // 3. spec.requiredConfigKeys 必须在 editor.fields 中存在（非 hidden 场景）
            spec.requiredConfigKeys.forEach { requiredKey ->
                assertTrue(
                    editor.fields.any { it.key == requiredKey },
                    "节点 [$type] 的 requiredConfigKey [$requiredKey] 在 editor.fields 中不存在"
                )
            }

            // 4. 端口规范性检查
            val inputPortIds = mutableSetOf<String>()
            spec.inputPorts.forEach { port ->
                assertTrue(port.id.isNotBlank(), "节点 [$type] 包含空 id 的输入端口")
                assertTrue(port.kind.isNotBlank(), "节点 [$type] 输入端口 [${port.id}] kind 不能为空")
                assertEquals(
                    ActionPortDirection.INPUT,
                    port.direction,
                    "节点 [$type] 的 inputPorts 端口 [${port.id}] direction 必须为 input"
                )
                assertFalse(
                    inputPortIds.contains(port.id),
                    "节点 [$type] 存在重复的输入端口 id: [${port.id}]"
                )
                inputPortIds.add(port.id)
            }

            val outputPortIds = mutableSetOf<String>()
            spec.outputPorts.forEach { port ->
                assertTrue(port.id.isNotBlank(), "节点 [$type] 包含空 id 的输出端口")
                assertTrue(port.kind.isNotBlank(), "节点 [$type] 输出端口 [${port.id}] kind 不能为空")
                assertEquals(
                    ActionPortDirection.OUTPUT,
                    port.direction,
                    "节点 [$type] 的 outputPorts 端口 [${port.id}] direction 必须为 output"
                )
                assertFalse(
                    outputPortIds.contains(port.id),
                    "节点 [$type] 存在重复的输出端口 id: [${port.id}]"
                )
                outputPortIds.add(port.id)
            }
        }
    }

    @Test
    fun allCategoriesAreDocumentedAndConfigured() {
        val runtime = createTestRuntime()
        val manifest = runtime.registry.toEditorManifest()
        val allConfiguredSpecs = com.xiaoyv.workflow.node.core.ActionNodeCategory.allSpecs

        assertTrue(allConfiguredSpecs.isNotEmpty(), "ActionNodeCategory.allSpecs 必须包含分类定义")

        // 1. 验证所有内置分类具备非空的中文 label 与说明 description
        allConfiguredSpecs.forEach { (id, spec) ->
            assertEquals(id, spec.id, "分类 Key 与 Spec.id 必须一致")
            assertTrue(spec.label.isNotBlank(), "分类 [$id] 必须配置非空的中文 label")
            assertTrue(spec.description.isNotBlank(), "分类 [$id] 必须配置非空的中文 description")
        }

        // 2. 验证所有已注册节点使用的 category 都已在 ActionNodeCategory.allSpecs 中配置
        val allNodeCategories = runtime.registry.all().map { it.spec.category }.toSet()
        allNodeCategories.forEach { categoryId ->
            assertTrue(
                allConfiguredSpecs.containsKey(categoryId),
                "节点使用的分类 [$categoryId] 未在 ActionNodeCategory.allSpecs 中配置，请补充中文名称与描述"
            )
        }

        // 3. 验证 manifest 导出的 categories 列表中携带正确的 label
        assertTrue(manifest.categories.isNotEmpty(), "导出的 Manifest 必须包含 categories")
        manifest.categories.forEach { categoryManifest ->
            assertTrue(categoryManifest.id.isNotBlank(), "导出的 category.id 不能为空")
            assertTrue(categoryManifest.label.isNotBlank(), "导出的 category.label 不能为空")
            assertEquals(
                allConfiguredSpecs[categoryManifest.id]?.label ?: categoryManifest.id,
                categoryManifest.label,
                "导出的 category [${categoryManifest.id}] 的 label 与配置不一致"
            )
        }
    }

    @Test
    fun defaultManifestExporterExportsValidJson() {
        val exportedJson = DefaultManifestExporter.generateManifestJson()
        assertTrue(exportedJson.isNotBlank())
        assertTrue(exportedJson.contains("\"nodeTypes\""))
        assertTrue(exportedJson.contains("\"categories\""))
        assertTrue(exportedJson.contains("\"capabilities\""))
        assertTrue(exportedJson.contains("flow.start"))
    }
}

