package com.xiaoyv.workflow.demo

import com.xiaoyv.workflow.demo.business.WorkflowsViewModel
import com.xiaoyv.workflow.demo.repository.ActionWorkflowRepository
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine

/**
 * 显式创建示例页面的 ViewModel，避免绑定任何 DI 框架。
 */
fun createWorkflowsViewModel(
    repository: ActionWorkflowRepository,
    engine: ActionWorkflowEngine,
): WorkflowsViewModel = WorkflowsViewModel(repository, engine)
