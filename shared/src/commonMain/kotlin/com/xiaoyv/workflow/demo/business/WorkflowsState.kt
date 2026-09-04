package com.xiaoyv.workflow.demo.business

import androidx.compose.runtime.Immutable
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.util.serialization.SerializeList
import kotlinx.collections.immutable.persistentListOf

/**
 * [WorkflowsState]
 *
 * @author why
 * @since 2025/1/12
 */
@Immutable
data class WorkflowsState(
    val workflows: SerializeList<ActionWorkflow> = persistentListOf(),
    val exampleRun: WorkflowExampleRunState? = null,
)

/**
 * 示例工作流最近一次执行的可展示状态。
 */
@Immutable
data class WorkflowExampleRunState(
    val workflowId: String? = null,
    val status: String,
    val executedNodeIds: SerializeList<String> = persistentListOf(),
    val resolvedUrl: String? = null,
    val errorMessage: String? = null,
    val outputLines: SerializeList<String> = persistentListOf(),
)
