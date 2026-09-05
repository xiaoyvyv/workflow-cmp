package com.xiaoyv.workflow.editor.bridge

import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeApi
import com.xiaoyv.workflow.editor.bridge.contract.EditorBridgeEvent
import com.xiaoyv.workflow.editor.bridge.contract.EditorDeviceDescriptor
import com.xiaoyv.workflow.editor.bridge.contract.EditorManifestResponse
import com.xiaoyv.workflow.editor.bridge.contract.EditorRunRequest
import com.xiaoyv.workflow.editor.bridge.contract.EditorSaveConflictResponse
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.SaveWorkflowRequest
import com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

/**
 * Multiplatform HTTP/WebSocket 适配器。工作流语义由服务层负责，本类仅负责传输映射。
 * 支持 JVM、Android、iOS 等全平台。
 */
class EditorBridgeServer(
    private val service: EditorWorkflowService,
    private val device: EditorDeviceDescriptor,
    private val json: Json,
    private val runController: EditorEngineRunController? = null,
    /**
     * 非回环或局域网监听时必填；通过 `Authorization: Bearer <token>` 传递。
     */
    private val accessToken: String? = null,
) {
    private val events =
        MutableSharedFlow<EditorBridgeEvent>(extraBufferCapacity = DEFAULT_EVENT_BUFFER_CAPACITY)
    private val eventSequence = atomic(0L)

    init {
        runController?.setEventPublisher(::publish)
    }

    fun publish(event: EditorBridgeEvent) {
        events.tryEmit(event.copy(sequence = eventSequence.incrementAndGet()))
    }

    fun Application.installRoutes() {
        install(ContentNegotiation) { json(json) }
        install(WebSockets)
        // 编辑器可被 Bridge 托管，也可由本地文件或其他静态站点独立托管。
        // 鉴权仍由下方的 accessToken 拦截器统一处理，CORS 不授予执行权限。
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Put)
        }
        if (accessToken != null)
            intercept(ApplicationCallPipeline.Plugins) {
                // 浏览器 WebSocket 无法附加 Authorization；仅允许在升级请求的
                // 查询参数中传递令牌。HTTP 调用方必须使用 Authorization 请求头。
                val supplied =
                    call.request
                        .header(EditorBridgeApi.AUTHORIZATION_HEADER)
                        ?.removePrefix(EditorBridgeApi.BEARER_PREFIX)
                        ?: if (call.request.uri.substringBefore('?') == EditorBridgeApi.EVENTS_PATH)
                            call.request.queryParameters[EditorBridgeApi.EVENT_ACCESS_TOKEN_QUERY]
                        else null
                if (supplied != accessToken) {
                    call.respond(HttpStatusCode.Unauthorized)
                    finish()
                }
            }
        routing {
            get(EditorBridgeApi.DEVICE_PATH) { call.respond(device) }
            get(EditorBridgeApi.MANIFEST_PATH) {
                call.respond(
                    EditorManifestResponse(json.decodeFromString(service.exportManifest()))
                )
            }
            get(EditorBridgeApi.WORKFLOW_PATH) {
                val document =
                    service.getWorkflow(call.parameters["id"].orEmpty())
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(document)
            }
            post(EditorBridgeApi.VALIDATE_WORKFLOW_PATH) {
                call.respond(service.validate(call.receive<ValidateWorkflowRequest>()))
            }
            put(EditorBridgeApi.WORKFLOW_PATH) {
                when (
                    val result =
                        service.save(
                            call.parameters["id"].orEmpty(),
                            call.receive<SaveWorkflowRequest>(),
                        )
                ) {
                    is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Saved ->
                        call.respond(HttpStatusCode.OK, result.document)

                    is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Conflict ->
                        call.respond(
                            HttpStatusCode.Conflict,
                            EditorSaveConflictResponse(result.current),
                        )

                    is com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult.Invalid ->
                        call.respond(HttpStatusCode.UnprocessableEntity, result.response)
                }
            }
            post(EditorBridgeApi.START_RUN_PATH) {
                val controller =
                    runController ?: return@post call.respond(HttpStatusCode.NotImplemented)
                val result =
                    controller.start(
                        call.parameters["id"].orEmpty(),
                        call.receive<EditorRunRequest>().revision,
                    ) ?: return@post call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.Accepted, result)
            }
            get(EditorBridgeApi.RUN_STATUS_PATH) {
                val controller =
                    runController ?: return@get call.respond(HttpStatusCode.NotImplemented)
                val status =
                    controller.status(call.parameters["runId"].orEmpty())
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(status)
            }
            post(EditorBridgeApi.CANCEL_RUN_PATH) {
                val controller =
                    runController ?: return@post call.respond(HttpStatusCode.NotImplemented)
                if (controller.cancel(call.parameters["runId"].orEmpty()))
                    call.respond(HttpStatusCode.Accepted)
                else call.respond(HttpStatusCode.NotFound)
            }
            webSocket(EditorBridgeApi.EVENTS_PATH) {
                events.asSharedFlow().collect { event ->
                    send(Frame.Text(json.encodeToString(EditorBridgeEvent.serializer(), event)))
                }
            }
            installStaticEditorRoutes()
        }
    }
}

/**
 * 启动全平台本地设备桥接服务；使用 Ktor 全平台 CIO 引擎。
 */
fun startEditorBridge(
    bridge: EditorBridgeServer,
    host: String = DEFAULT_BIND_HOST,
    port: Int = 0,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> =
    embeddedServer(CIO, host = host, port = port) {
        bridge.run { installRoutes() }
    }.start(wait = false)

private const val DEFAULT_EVENT_BUFFER_CAPACITY = 128
private const val DEFAULT_BIND_HOST = "0.0.0.0"
