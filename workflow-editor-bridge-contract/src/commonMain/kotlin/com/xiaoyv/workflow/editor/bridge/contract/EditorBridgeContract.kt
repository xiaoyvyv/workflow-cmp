package com.xiaoyv.workflow.editor.bridge.contract

import com.xiaoyv.workflow.codec.ActionEditorManifest
import com.xiaoyv.workflow.engine.ActionWorkflowValidation
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import kotlinx.serialization.Serializable

/**
 * HTTP/WebSocket 桥接层与 MCP 适配器共享的、带版本的传输无关 DTO。
 */
object EditorBridgeProtocol {
    const val VERSION = 1
    const val UNASSIGNED_EVENT_SEQUENCE = 0L
}

/**
 * 设备桥接客户端与服务端适配器共享的稳定 HTTP 与 WebSocket 路径。
 */
object EditorBridgeApi {
    private const val ROOT = "/api/v1"

    const val DEVICE_PATH = "$ROOT/device"
    const val MANIFEST_PATH = "$ROOT/manifest"
    const val WORKFLOW_PATH = "$ROOT/workflows/{id}"
    const val VALIDATE_WORKFLOW_PATH = "$ROOT/workflows/validate"
    const val START_RUN_PATH = "$ROOT/workflows/{id}/runs"
    const val RUN_STATUS_PATH = "$ROOT/runs/{runId}"
    const val CANCEL_RUN_PATH = "$ROOT/runs/{runId}/cancel"
    const val EVENTS_PATH = "$ROOT/events"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    const val EVENT_ACCESS_TOKEN_QUERY = "access_token"
}

/**
 * 字符串值必须保持稳定，因为会序列化到桥接 DTO。
 */
object EditorBridgeRunStatusValue {
    const val RUNNING = "running"
    const val COMPLETED = "completed"
    const val FAILED = "failed"
    const val CANCELLED = "cancelled"
}

/**
 * 字符串值必须保持稳定，因为浏览器与 MCP 消费方会据此渲染。
 */
object EditorBridgeEventType {
    const val RUN_STARTED = "run.started"
    const val RUN_COMPLETED = "run.completed"
    const val RUN_FAILED = "run.failed"
    const val NODE_STARTED = "node.started"
    const val NODE_COMPLETED = "node.completed"
    const val SIDE_EFFECT_REQUESTED = "side-effect.requested"
}

@Serializable
data class EditorWorkflowDocument(
    val workflow: ActionWorkflow,
    val revision: Long,
)

@Serializable
data class SaveWorkflowRequest(
    val baseRevision: Long,
    val workflow: ActionWorkflow,
)

/**
 * 可安全传输给 HTTP/MCP 客户端的乐观锁失败表示。
 */
@Serializable
data class EditorSaveConflictResponse(val current: EditorWorkflowDocument?)

@Serializable
data class ValidateWorkflowRequest(val workflow: ActionWorkflow)

@Serializable
data class ValidateWorkflowResponse(
    val workflow: ActionWorkflow,
    val validation: ActionWorkflowValidation,
)

@Serializable
data class EditorDeviceDescriptor(
    val protocolVersion: Int = EditorBridgeProtocol.VERSION,
    val name: String,
    val version: String? = null,
    val bridgeStatus: String = EditorBridgeRunStatusValue.RUNNING,
    val availableCapabilities: Set<String> = emptySet(),
)

@Serializable
data class EditorRunRequest(val revision: Long? = null)

@Serializable
data class EditorRunDescriptor(
    val runId: String,
    val workflowId: String,
    val revision: Long,
    val status: String,
)

/**
 * 供重连编辑器与 MCP 客户端使用的、容量受限的设备端运行历史。
 */
@Serializable
data class EditorRunStatus(
    val run: EditorRunDescriptor,
    val events: List<EditorBridgeEvent> = emptyList(),
)

/**
 * 可安全传输的事件；敏感载荷必须由设备适配器脱敏。
 */
@Serializable
data class EditorBridgeEvent(
    val sequence: Long,
    val type: String,
    val timestampMillis: Long,
    val runId: String? = null,
    val nodeId: String? = null,
    val message: String = "",
)

@Serializable
data class EditorManifestResponse(val manifest: ActionEditorManifest)
