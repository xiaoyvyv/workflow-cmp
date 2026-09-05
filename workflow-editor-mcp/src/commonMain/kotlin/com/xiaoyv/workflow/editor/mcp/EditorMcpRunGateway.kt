package com.xiaoyv.workflow.editor.mcp

import com.xiaoyv.workflow.editor.bridge.EditorEngineRunController
import com.xiaoyv.workflow.editor.bridge.contract.EditorRunStatus

/**
 * MCP 执行工具与设备端运行控制器之间的组合适配器。全平台通用。
 */
class EditorMcpRunGateway(private val controller: EditorEngineRunController) :
    WorkflowMcpRunGateway {
    override suspend fun start(
        workflowId: String,
        revision: Long?,
    ): WorkflowMcpRunResult =
        controller.start(workflowId, revision)?.let { descriptor ->
            WorkflowMcpRunResult.Started(
                runId = descriptor.runId,
                workflowId = descriptor.workflowId,
                revision = descriptor.revision,
            )
        } ?: WorkflowMcpRunResult.NotFound

    override suspend fun cancel(runId: String): Boolean = controller.cancel(runId)

    override suspend fun status(runId: String): EditorRunStatus? = controller.status(runId)
}
