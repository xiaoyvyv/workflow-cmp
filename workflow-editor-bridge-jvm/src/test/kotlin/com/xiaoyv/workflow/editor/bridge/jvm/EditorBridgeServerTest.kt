package com.xiaoyv.workflow.editor.bridge.jvm

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeApi
import com.xiaoyv.workflow.editor.bridge.contract.EditorDeviceDescriptor
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.InMemoryEditorWorkflowRepository
import com.xiaoyv.workflow.editor.bridge.contract.SaveWorkflowRequest
import com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowRequest
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.node.core.ActionNodeSpec
import com.xiaoyv.workflow.util.defaultJson
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.collections.immutable.toPersistentList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorBridgeServerTest {
    @Test
    fun exposesDeviceAndManifestEndpoints() = testApplication {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.node",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "测试节点"),
                            ),
                        executor = { _, _ -> error("not executed") },
                    )
                )
            )
        val bridge =
            EditorBridgeServer(
                service =
                    EditorWorkflowService(
                        registry,
                        ActionWorkflowValidator(registry),
                        InMemoryEditorWorkflowRepository(),
                        ActionEditorManifestCodec(defaultJson, registry),
                    ),
                device = EditorDeviceDescriptor(name = "test-device"),
                json = defaultJson,
            )
        application { bridge.run { installRoutes() } }

        val device = client.get(EditorBridgeApi.DEVICE_PATH)
        val manifest = client.get(EditorBridgeApi.MANIFEST_PATH)
        val editor = client.get("/")

        assertEquals(HttpStatusCode.OK, device.status)
        assertTrue(device.bodyAsText().contains("test-device"))
        assertEquals(HttpStatusCode.OK, manifest.status)
        assertTrue(manifest.bodyAsText().contains("test.node"))
        assertEquals(HttpStatusCode.OK, editor.status)
        assertTrue(editor.bodyAsText().contains("Workflow Editor"))
    }

    @Test
    fun validatesSavesLoadsAndRejectsStaleRevisions() = testApplication {
        val registry =
            ActionNodeRegistry(
                listOf(
                    ActionNodeDefinition(
                        spec =
                            ActionNodeSpec(
                                type = "test.node",
                                category = "test",
                                editor = ActionNodeEditorSpec(title = "测试节点"),
                            ),
                        executor = { _, _ -> error("not executed") },
                    )
                )
            )
        val bridge =
            EditorBridgeServer(
                service =
                    EditorWorkflowService(
                        registry,
                        ActionWorkflowValidator(registry),
                        InMemoryEditorWorkflowRepository(),
                        ActionEditorManifestCodec(defaultJson, registry),
                    ),
                device = EditorDeviceDescriptor(name = "test-device"),
                json = defaultJson,
            )
        application { bridge.run { installRoutes() } }
        val workflow =
            ActionWorkflow(
                id = "demo",
                name = "Demo",
                entryNodeId = "entry",
                nodes = listOf(ActionNode("entry", "test.node")).toPersistentList(),
            )
        val validation =
            client.post(EditorBridgeApi.VALIDATE_WORKFLOW_PATH) {
                contentType(ContentType.Application.Json)
                setBody(
                    defaultJson.encodeToString(
                        ValidateWorkflowRequest.serializer(),
                        ValidateWorkflowRequest(workflow),
                    )
                )
            }
        val saved =
            client.put(workflowPath("demo")) {
                contentType(ContentType.Application.Json)
                setBody(
                    defaultJson.encodeToString(
                        SaveWorkflowRequest.serializer(),
                        SaveWorkflowRequest(0, workflow),
                    )
                )
            }
        val loaded = client.get(workflowPath("demo"))
        val conflict =
            client.put(workflowPath("demo")) {
                contentType(ContentType.Application.Json)
                setBody(
                    defaultJson.encodeToString(
                        SaveWorkflowRequest.serializer(),
                        SaveWorkflowRequest(0, workflow),
                    )
                )
            }

        assertEquals(HttpStatusCode.OK, validation.status)
        val validationBody = validation.bodyAsText()
        assertTrue(validationBody.contains("isValid"), validationBody)
        assertEquals(HttpStatusCode.OK, saved.status)
        assertTrue(saved.bodyAsText().contains("\"revision\":1"))
        assertEquals(HttpStatusCode.OK, loaded.status)
        assertEquals(HttpStatusCode.Conflict, conflict.status)
    }

    @Test
    fun rejectsUnauthenticatedRequestsWhenLanTokenIsConfigured() = testApplication {
        val registry = ActionNodeRegistry(emptyList())
        val bridge =
            EditorBridgeServer(
                service =
                    EditorWorkflowService(
                        registry,
                        ActionWorkflowValidator(registry),
                        InMemoryEditorWorkflowRepository(),
                        ActionEditorManifestCodec(defaultJson, registry),
                    ),
                device = EditorDeviceDescriptor(name = "test-device"),
                json = defaultJson,
                accessToken = "pairing-token",
            )
        application { bridge.run { installRoutes() } }

        assertEquals(HttpStatusCode.Unauthorized, client.get(EditorBridgeApi.DEVICE_PATH).status)
        assertEquals(
            HttpStatusCode.OK,
            client
                .get(EditorBridgeApi.DEVICE_PATH) {
                    header(
                        EditorBridgeApi.AUTHORIZATION_HEADER,
                        "${EditorBridgeApi.BEARER_PREFIX}pairing-token",
                    )
                }
                .status,
        )
    }

    @Test
    fun allowsEditorHostedOnAnotherOriginToCallBridge() = testApplication {
        val registry = ActionNodeRegistry(emptyList())
        val bridge =
            EditorBridgeServer(
                service =
                    EditorWorkflowService(
                        registry,
                        ActionWorkflowValidator(registry),
                        InMemoryEditorWorkflowRepository(),
                        ActionEditorManifestCodec(defaultJson, registry),
                    ),
                device = EditorDeviceDescriptor(name = "test-device"),
                json = defaultJson,
            )
        application { bridge.run { installRoutes() } }

        val response =
            client.options(EditorBridgeApi.MANIFEST_PATH) {
                header(HttpHeaders.Origin, EDITOR_ORIGIN)
                header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Put.value)
            }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(CORS_ANY_ORIGIN, response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    private fun workflowPath(id: String) = EditorBridgeApi.WORKFLOW_PATH.replace("{id}", id)

    private companion object {
        const val EDITOR_ORIGIN = "https://editor.example"
        const val CORS_ANY_ORIGIN = "*"
    }
}
