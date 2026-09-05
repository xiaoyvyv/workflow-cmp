package com.xiaoyv.workflow.codec

import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldCondition
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldValidation
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.node.core.ActionPortDirection
import com.xiaoyv.workflow.node.core.ActionPortSpec
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionEditorManifestCodecTest {
    @Test
    fun exportsRegisteredEditorSpecWithoutExecutors() {
        val registry =
            ActionNodeRegistry(
                listOf(
                    definition(
                        "demo.editor",
                        editor =
                            ActionNodeEditorSpec(
                                title = "Demo",
                                defaultConfig = buildJsonObject { put("count", JsonPrimitive(1)) },
                                fields =
                                    persistentListOf(
                                        ActionConfigFieldSpec(
                                            "count",
                                            kind = ActionEditorFieldKind.NUMBER,
                                            required = true,
                                        )
                                    ),
                            ),
                        requiredKeys = emptySet(),
                    )
                )
            )

        val raw = ActionEditorManifestCodec(defaultJson, registry).export()
        val manifest = defaultJson.decodeFromString(ActionEditorManifest.serializer(), raw)
        val node = manifest.nodeTypes.single()

        assertEquals("demo.editor", node.type)
        assertEquals(ActionWorkflow.CURRENT_FORMAT_VERSION, manifest.workflowFormatVersion)
        assertEquals(listOf("demo.editor"), manifest.categories.single().nodeTypes)
        assertEquals("Demo", node.editor.title)
        assertEquals(JsonPrimitive(1), node.editor.defaultConfig["count"])
        assertEquals(ActionEditorFieldKind.NUMBER, node.editor.fields.single().kind)
        assertFalse(raw.contains("executor"))
    }

    @Test
    fun suppliesDerivedEditorSpecForNodeWithoutAnOverride() {
        val manifest = ActionNodeRegistry(listOf(definition("demo.legacy"))).toEditorManifest()
        val node = manifest.nodeTypes.single()

        assertEquals("demo.legacy", node.editor.title)
        assertEquals(listOf("required"), node.editor.fields.map { it.key })
        assertTrue(node.editor.fields.single().required)
    }

    @Test
    fun sortsNodesAndPortsByStableEditorOrder() {
        val registry =
            ActionNodeRegistry(
                listOf(
                    definition("z.type"),
                    definition(
                        "a.type",
                        ports =
                            persistentListOf(
                                ActionPortSpec(
                                    "later",
                                    direction = ActionPortDirection.OUTPUT,
                                    order = 2,
                                ),
                                ActionPortSpec(
                                    "first",
                                    direction = ActionPortDirection.OUTPUT,
                                    order = 1,
                                ),
                            ),
                    ),
                )
            )

        val manifest = registry.toEditorManifest()

        assertEquals(listOf("a.type", "z.type"), manifest.nodeTypes.map { it.type })
        assertEquals(listOf("first", "later"), manifest.nodeTypes.first().outputPorts.map { it.id })
    }

    @Test
    fun roundTripsOfflineBundle() {
        val workflow =
            ActionWorkflow(
                id = "sample",
                name = "Sample",
                entryNodeId = "node",
                nodes = listOf(ActionNode("node", "demo.editor")).toPersistentList(),
            )
        val bundle =
            ActionEditorBundle(
                manifest = ActionNodeRegistry(listOf(definition("demo.editor"))).toEditorManifest(),
                workflow = workflow,
            )

        val decoded =
            ActionEditorBundleCodec(defaultJson)
                .import(ActionEditorBundleCodec(defaultJson).export(bundle))

        assertEquals(bundle, decoded)
    }

    @Test
    fun rejectsExplicitEditorSpecsThatCannotDescribeRequiredConfiguration() {
        val registry =
            ActionNodeRegistry(
                listOf(definition("demo.invalid", editor = ActionNodeEditorSpec(title = "Invalid")))
            )
        val validation = registry.validateEditorSpecs()

        assertFalse(validation.isValid)
        assertTrue(validation.issues.single().contains("required config 'required'"))
        kotlin.test.assertFails { ActionEditorManifestCodec(defaultJson, registry).export() }
    }

    @Test
    fun exportsDeclarativeFieldConditionsAndValidation() {
        val field =
            ActionConfigFieldSpec(
                key = "advanced",
                visibleWhen = ActionEditorFieldCondition("enabled", JsonPrimitive(true)),
                validation = ActionEditorFieldValidation(minimum = 0.0, maximum = 10.0),
            )
        val registry =
            ActionNodeRegistry(
                listOf(
                    definition(
                        "demo.conditions",
                        ActionNodeEditorSpec("Conditions", fields = persistentListOf(field)),
                        requiredKeys = emptySet(),
                    )
                )
            )
        val manifest =
            defaultJson.decodeFromString(
                ActionEditorManifest.serializer(),
                ActionEditorManifestCodec(defaultJson, registry).export(),
            )
        val exported = manifest.nodeTypes.single().editor.fields.single()

        assertEquals(field.visibleWhen, exported.visibleWhen)
        assertEquals(field.validation, exported.validation)
    }

    @Test
    fun rejectsSensitiveFieldDefaultsBeforeTheyCanReachAManifest() {
        val editor =
            ActionNodeEditorSpec(
                title = "Sensitive",
                defaultConfig = buildJsonObject { put("secret", JsonPrimitive("value")) },
                fields = persistentListOf(ActionConfigFieldSpec("secret", sensitive = true)),
            )

        val validation =
            ActionNodeRegistry(
                listOf(definition("demo.sensitive", editor, requiredKeys = emptySet()))
            )
                .validateEditorSpecs()

        assertFalse(validation.isValid)
        assertTrue(validation.issues.single().contains("sensitive field 'secret'"))
    }

    private fun definition(
        type: String,
        editor: ActionNodeEditorSpec? = null,
        ports: kotlinx.collections.immutable.PersistentList<ActionPortSpec> = persistentListOf(),
        requiredKeys: Set<String> = setOf("required"),
    ) =
        ActionNodeDefinition(
            spec =
                ActionNodeSpec(
                    type = type,
                    category = "test",
                    outputPorts = ports,
                    requiredConfigKeys = requiredKeys,
                    editor =
                        editor
                            ?: ActionNodeEditorSpec(
                                title = type,
                                fields =
                                    requiredKeys
                                        .sorted()
                                        .map { key ->
                                            ActionConfigFieldSpec(
                                                key = key,
                                                required = true,
                                            )
                                        }
                                        .toPersistentList(),
                            ),
                ),
            executor = { _, _ -> error("not executed") },
        )
}
