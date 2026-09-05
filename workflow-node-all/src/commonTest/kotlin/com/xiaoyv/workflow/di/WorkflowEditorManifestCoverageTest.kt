package com.xiaoyv.workflow.di

import com.xiaoyv.workflow.codec.toEditorManifest
import com.xiaoyv.workflow.codec.validateEditorSpecs
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkflowEditorManifestCoverageTest {
    @Test
    fun exportsAnEditorSpecForEveryBuiltInNode() {
        val runtime =
            createWorkflowRuntime(
                WorkflowRuntimeConfig(
                    httpRequestExecutor = ActionHttpRequestExecutor { buildJsonObject {} }
                )
            )

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
}
