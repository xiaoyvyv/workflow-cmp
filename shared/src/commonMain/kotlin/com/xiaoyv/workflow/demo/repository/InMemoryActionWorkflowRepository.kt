package com.xiaoyv.workflow.demo.repository

import com.xiaoyv.workflow.codec.ActionWorkflowCodec
import com.xiaoyv.workflow.codec.ActionWorkflowImportResult
import com.xiaoyv.workflow.engine.ActionWorkflowValidation
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * 示例页面使用的内存工作流仓储，不持久化宿主数据。
 */
class InMemoryActionWorkflowRepository(
    private val validator: ActionWorkflowValidator,
    private val codec: ActionWorkflowCodec,
) : ActionWorkflowRepository {
    private var workflows: ImmutableList<ActionWorkflow> = persistentListOf()

    override fun list(): ImmutableList<ActionWorkflow> = workflows

    override fun find(id: String): ActionWorkflow? = workflows.firstOrNull { workflow -> workflow.id == id }

    override fun save(workflow: ActionWorkflow): ActionWorkflowValidation {
        val validation = validator.validate(workflow)
        if (validation.isValid) {
            workflows = workflows.filterNot { item -> item.id == workflow.id }.plus(workflow).toPersistentList()
        }
        return validation
    }

    override fun delete(id: String) {
        workflows = workflows.filterNot { workflow -> workflow.id == id }.toPersistentList()
    }

    override fun export(id: String): String? = find(id)?.let(codec::export)

    override fun import(raw: String, overwrite: Boolean): ActionWorkflowImportResult {
        return when (val result = codec.import(raw)) {
            is ActionWorkflowImportResult.Success -> {
                if (!overwrite && find(result.workflow.id) != null) {
                    ActionWorkflowImportResult.Failure("duplicate_workflow_id", "已存在相同 ID 的工作流")
                } else {
                    save(result.workflow)
                    result
                }
            }

            else -> result
        }
    }
}
