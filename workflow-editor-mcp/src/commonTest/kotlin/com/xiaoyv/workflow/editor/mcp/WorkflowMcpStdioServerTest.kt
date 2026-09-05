package com.xiaoyv.workflow.editor.mcp

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkflowMcpStdioServerTest {
    private val server =
        WorkflowMcpStdioServer(
            WorkflowMcpService(
                EditorWorkflowService(
                    ActionNodeRegistry(emptyList()),
                    ActionWorkflowValidator(ActionNodeRegistry(emptyList())),
                    InMemoryEditorWorkflowRepository(),
                    ActionEditorManifestCodec(defaultJson, ActionNodeRegistry(emptyList())),
                ),
                defaultJson,
                "# Architecture",
            ),
            defaultJson,
        )

    @Test
    fun listsResources() {
        val response =
            defaultJson.parseToJsonElement(server.handle(request("resources/list"))!!).jsonObject
        assertEquals(
            "workflow://architecture",
            response["result"]!!
                .jsonObject["resources"]!!
                .jsonArray
                .first()
                .jsonObject["uri"]!!
                .jsonPrimitive
                .content,
        )
    }

    @Test
    fun rejectsUnknownTool() {
        val response = server.handle(request("tools/call", """{"name":"unknown"}"""))!!
        assertTrue(response.contains("-32602"))
    }

    @Test
    fun notificationsHaveNoResponse() {
        assertEquals(
            null,
            server.handle(notification("notifications/initialized")),
        )
    }

    @Test
    fun exposesCreateAndUpdateTools() {
        val response = server.handle(request("tools/list"))!!
        assertTrue(response.contains("workflow_create"))
        assertTrue(response.contains("workflow_update"))
        assertTrue(response.contains("workflow_get"))
    }

    @Test
    fun createToolReturnsStructuredValidationFailure() {
        val arguments =
            """
            {
              "name": "workflow_create",
              "arguments": {
                "id": "demo",
                "workflow": {
                  "id": "demo",
                  "name": "Demo",
                  "entryNodeId": "missing"
                }
              }
            }
            """
                .trimIndent()
        val response = server.handle(request("tools/call", arguments))!!
        assertTrue(response.contains("structuredContent"))
        assertTrue(response.contains("isError"))
    }

    @Test
    fun runAndCancelReportUnavailableWhenDeviceHasNoRunGateway() {
        val run =
            server.handle(
                request("tools/call", """{"name":"workflow_run","arguments":{"id":"demo"}}""")
            )!!
        val cancel =
            server.handle(
                request("tools/call", """{"name":"run_cancel","arguments":{"runId":"run"}}""")
            )!!

        assertTrue(run.contains("unavailable"))
        assertTrue(cancel.contains("isError"))
    }

    @Test
    fun workflowResourceTemplatesAreAdvertised() {
        val response = server.handle(request("resources/templates/list"))!!

        assertTrue(response.contains("workflow://workflows/{id}"))
        assertTrue(response.contains("workflow://nodes/{type}"))
    }

    @Test
    fun exposesDocumentedToolNameAliases() {
        val response = server.handle(request("tools/list"))!!

        assertTrue(response.contains("list_node_types"))
        assertTrue(response.contains("validate_workflow"))
        assertTrue(response.contains("get_run_status"))
    }

    @Test
    fun documentedListNodesToolUsesSharedManifest() {
        val response =
            server.handle(request("tools/call", """{"name":"list_node_types","arguments":{}}"""))!!

        assertTrue(response.contains("Node types"))
    }

    private fun request(method: String, params: String = "{}") =
        """
        {"jsonrpc":"2.0","id":1,"method":"$method","params":$params}
        """
            .trimIndent()

    private fun notification(method: String, params: String = "{}") =
        """
        {"jsonrpc":"2.0","method":"$method","params":$params}
        """
            .trimIndent()
}
