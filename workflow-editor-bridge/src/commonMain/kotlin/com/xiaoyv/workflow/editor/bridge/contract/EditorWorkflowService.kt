package com.xiaoyv.workflow.editor.bridge.contract

import com.xiaoyv.workflow.codec.ActionEditorManifestCodec
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * HTTP 与 MCP 适配器使用的持久化边界。
 */
interface EditorWorkflowRepository {
    suspend fun get(id: String): EditorWorkflowDocument?

    suspend fun save(id: String, expectedRevision: Long, workflow: ActionWorkflow): EditorSaveResult
}

sealed interface EditorSaveResult {
    data class Saved(val document: EditorWorkflowDocument) : EditorSaveResult

    data class Conflict(val current: EditorWorkflowDocument?) : EditorSaveResult

    data class Invalid(val response: ValidateWorkflowResponse) : EditorSaveResult
}

/**
 * 供桌面演示与测试使用的线程安全内存仓库；生产宿主可替换实现。
 */
class InMemoryEditorWorkflowRepository : EditorWorkflowRepository {
    private val mutex = Mutex()
    private val documents = mutableMapOf<String, EditorWorkflowDocument>()

    override suspend fun get(id: String): EditorWorkflowDocument? = mutex.withLock { documents[id] }

    override suspend fun save(
        id: String,
        expectedRevision: Long,
        workflow: ActionWorkflow,
    ): EditorSaveResult = mutex.withLock {
        val current = documents[id]
        if (current != null && current.revision != expectedRevision) {
            return@withLock EditorSaveResult.Conflict(current)
        }
        val nextRevision = if (current != null) {
            expectedRevision + 1
        } else {
            if (expectedRevision <= 0L) 1L else expectedRevision + 1L
        }
        EditorSaveResult.Saved(
            EditorWorkflowDocument(workflow, nextRevision).also { documents[id] = it }
        )
    }
}

/**
 * 共享业务层；传输层委托至此，避免重复实现校验与版本控制。
 */
class EditorWorkflowService(
    private val registry: ActionNodeRegistry,
    private val validator: ActionWorkflowValidator,
    private val repository: EditorWorkflowRepository,
    private val manifestCodec: ActionEditorManifestCodec,
) {
    fun exportManifest(): String = manifestCodec.export()

    suspend fun getWorkflow(id: String): EditorWorkflowDocument? = repository.get(id)

    fun validate(request: ValidateWorkflowRequest): ValidateWorkflowResponse {
        val migrated = registry.migrate(request.workflow)
        return ValidateWorkflowResponse(migrated, validator.validate(migrated))
    }

    suspend fun save(id: String, request: SaveWorkflowRequest): EditorSaveResult {
        val validated = validate(ValidateWorkflowRequest(request.workflow))
        if (!validated.validation.isValid) return EditorSaveResult.Invalid(validated)
        return repository.save(id, request.baseRevision, validated.workflow)
    }
}
