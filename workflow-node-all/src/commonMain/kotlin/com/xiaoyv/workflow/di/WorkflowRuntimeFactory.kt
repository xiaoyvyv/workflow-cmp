package com.xiaoyv.workflow.di

import com.xiaoyv.workflow.codec.ActionWorkflowCodec
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine
import com.xiaoyv.workflow.node.builtin.builtInActionNodeDefinitions
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import com.xiaoyv.workflow.port.ActionWorkflowFileStorage
import com.xiaoyv.workflow.port.ActionWorkflowLogger
import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore
import com.xiaoyv.workflow.util.defaultJson

/**
 * 不依赖 DI 框架的工作流运行时组件集合。
 */
data class WorkflowRuntime(
    val registry: ActionNodeRegistry,
    val validator: ActionWorkflowValidator,
    val codec: ActionWorkflowCodec,
    val engine: ActionWorkflowEngine,
)

/**
 * 显式创建内置节点所需的工作流运行时。
 */
fun createWorkflowRuntime(
    httpRequestExecutor: ActionHttpRequestExecutor,
    preferencesStore: ActionWorkflowPreferencesStore = ActionWorkflowPreferencesStore.Default,
    fileStorage: ActionWorkflowFileStorage = ActionWorkflowFileStorage.Default,
    logger: ActionWorkflowLogger = ActionWorkflowLogger.Default,
): WorkflowRuntime {
    val registry = ActionNodeRegistry(
        nodeDefinitions = builtInActionNodeDefinitions(
            httpRequestExecutor = httpRequestExecutor,
            preferencesStore = preferencesStore,
            fileStorage = fileStorage,
            logger = logger,
        ),
    )
    val validator = ActionWorkflowValidator(registry)
    val codec = ActionWorkflowCodec(defaultJson, validator, registry)
    val engine = ActionWorkflowEngine(
        registry = registry,
        validator = validator,
        now = { kotlin.time.Clock.System.now().toEpochMilliseconds() },
    )
    return WorkflowRuntime(registry, validator, codec, engine)
}
