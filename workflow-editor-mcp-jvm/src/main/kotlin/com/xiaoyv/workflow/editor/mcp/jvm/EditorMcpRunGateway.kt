package com.xiaoyv.workflow.editor.mcp.jvm

import com.xiaoyv.workflow.editor.bridge.contract.EditorRunStatus
import com.xiaoyv.workflow.editor.bridge.jvm.EditorEngineRunController
import com.xiaoyv.workflow.editor.mcp.WorkflowMcpRunGateway
import com.xiaoyv.workflow.editor.mcp.WorkflowMcpRunResult

/**
 * MCP 执行工具与设备端运行控制器之间的可选 JVM 组合适配器。
 *
 * 独立模块可避免 HTTP 桥接层依赖 MCP。
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
