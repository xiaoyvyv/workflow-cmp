package com.xiaoyv.workflow.editor.mcp

import com.xiaoyv.workflow.codec.ActionEditorManifest
import com.xiaoyv.workflow.editor.bridge.contract.EditorRunStatus
import com.xiaoyv.workflow.editor.bridge.contract.EditorSaveResult
import com.xiaoyv.workflow.editor.bridge.contract.EditorWorkflowService
import com.xiaoyv.workflow.editor.bridge.contract.SaveWorkflowRequest
import com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowRequest
import com.xiaoyv.workflow.editor.bridge.contract.ValidateWorkflowResponse
import kotlinx.serialization.json.Json

/**
 * 与传输无关的 MCP 领域适配器。MCP SDK 适配层可将这些方法暴露为资源与工具，
 * 无需重复维护节点目录或校验实现。
 */
class WorkflowMcpService(
    private val workflowService: EditorWorkflowService,
    private val json: Json,
    private val architecture: String,
    private val runGateway: WorkflowMcpRunGateway? = null,
) {
    fun readResource(uri: String): String =
        when {
            uri == ARCHITECTURE_URI -> architecture
            uri == MANIFEST_URI -> workflowService.exportManifest()
            uri.startsWith("workflow://categories/") ->
                categoryResource(uri.removePrefix("workflow://categories/"))

            uri.startsWith("workflow://workflows/") ->
                error("Workflow resources are asynchronous; use getWorkflow")

            uri.startsWith("workflow://nodes/") ->
                nodeResource(uri.removePrefix("workflow://nodes/"))

            else -> error("Unsupported workflow MCP resource: $uri")
        }

    fun validateWorkflow(rawWorkflow: String): ValidateWorkflowResponse {
        val workflow =
            json.decodeFromString(
                com.xiaoyv.workflow.model.definition.ActionWorkflow.serializer(),
                rawWorkflow,
            )
        return workflowService.validate(ValidateWorkflowRequest(workflow))
    }

    fun listNodeTypes(category: String? = null, query: String? = null): String {
        val manifest =
            json.decodeFromString(
                ActionEditorManifest.serializer(),
                workflowService.exportManifest(),
            )
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val nodes =
            manifest.nodeTypes.filter { node ->
                (category == null || node.category == category) &&
                        (normalizedQuery.isEmpty() ||
                                node.type.lowercase().contains(normalizedQuery) ||
                                node.editor.title.lowercase().contains(normalizedQuery))
            }
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(
                com.xiaoyv.workflow.codec.ActionEditorNodeManifest.serializer()
            ),
            nodes,
        )
    }

    fun getNodeSpec(type: String): String = nodeResource(type)

    /**
     * 保存操作必须显式调用：先由共享设备服务完成校验。
     */
    suspend fun saveWorkflow(
        id: String,
        baseRevision: Long,
        rawWorkflow: String,
    ): EditorSaveResult {
        val workflow =
            json.decodeFromString(
                com.xiaoyv.workflow.model.definition.ActionWorkflow.serializer(),
                rawWorkflow,
            )
        require(workflow.id == id) { "Workflow id must match the resource id" }
        return workflowService.save(id, SaveWorkflowRequest(baseRevision, workflow))
    }

    suspend fun getWorkflow(id: String) = workflowService.getWorkflow(id)

    suspend fun runWorkflow(id: String, revision: Long?): WorkflowMcpRunResult =
        runGateway?.start(id, revision) ?: WorkflowMcpRunResult.Unavailable

    suspend fun cancelRun(runId: String): Boolean = runGateway?.cancel(runId) ?: false

    suspend fun getRunStatus(runId: String): EditorRunStatus? = runGateway?.status(runId)

    private fun nodeResource(type: String): String {
        val manifest =
            json.decodeFromString(
                ActionEditorManifest.serializer(),
                workflowService.exportManifest(),
            )
        val node =
            manifest.nodeTypes.firstOrNull { it.type == type }
                ?: error("Unknown workflow node type: $type")
        return json.encodeToString(
            com.xiaoyv.workflow.codec.ActionEditorNodeManifest.serializer(),
            node,
        )
    }

    private fun categoryResource(category: String): String {
        val manifest =
            json.decodeFromString(
                ActionEditorManifest.serializer(),
                workflowService.exportManifest(),
            )
        val nodes = manifest.nodeTypes.filter { it.category == category }
        require(nodes.isNotEmpty()) { "Unknown workflow node category: $category" }
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(
                com.xiaoyv.workflow.codec.ActionEditorNodeManifest.serializer()
            ),
            nodes,
        )
    }

    companion object {
        const val ARCHITECTURE_URI = "workflow://architecture"
        const val MANIFEST_URI = "workflow://manifest"
    }
}

/**
 * 由设备宿主注入的执行边界；MCP 不持有引擎或副作用。
 */
interface WorkflowMcpRunGateway {
    suspend fun start(workflowId: String, revision: Long?): WorkflowMcpRunResult

    suspend fun cancel(runId: String): Boolean

    suspend fun status(runId: String): EditorRunStatus?
}

sealed interface WorkflowMcpRunResult {
    data class Started(val runId: String, val workflowId: String, val revision: Long) :
        WorkflowMcpRunResult

    data object NotFound : WorkflowMcpRunResult

    data object Unavailable : WorkflowMcpRunResult
}
