package com.xiaoyv.workflow.di

import com.xiaoyv.workflow.codec.ActionWorkflowCodec
import com.xiaoyv.workflow.engine.ActionWorkflowValidator
import com.xiaoyv.workflow.engine.runtime.ActionWorkflowEngine
import com.xiaoyv.workflow.node.builtin.builtInActionNodeDefinitions
import com.xiaoyv.workflow.node.core.ActionNodeDefinition
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import com.xiaoyv.workflow.port.ActionWorkflowFileStorage
import com.xiaoyv.workflow.port.ActionWorkflowLogger
import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore
import com.xiaoyv.workflow.util.defaultJson
import kotlinx.serialization.json.Json

/**
 * 不依赖 DI 框架的工作流运行时组件集合。
 */
data class WorkflowRuntime(
    val registry: ActionNodeRegistry,
    val validator: ActionWorkflowValidator,
    val codec: ActionWorkflowCodec,
    val engine: ActionWorkflowEngine,
    val config: WorkflowRuntimeConfig
)

data class WorkflowRuntimeConfig(
    val httpRequestExecutor: ActionHttpRequestExecutor,
    val preferencesStore: ActionWorkflowPreferencesStore = ActionWorkflowPreferencesStore.Default,
    val fileStorage: ActionWorkflowFileStorage = ActionWorkflowFileStorage.Default,
    val logger: ActionWorkflowLogger = ActionWorkflowLogger.Default,
    val json: Json = defaultJson,
)

/**
 * 显式创建内置节点所需的工作流运行时。
 */
fun createWorkflowRuntime(
    config: WorkflowRuntimeConfig,
    nodeDefinitions: List<ActionNodeDefinition> = emptyList(),
): WorkflowRuntime {
    val registry = ActionNodeRegistry(
        nodeDefinitions = builtInActionNodeDefinitions(
            httpRequestExecutor = config.httpRequestExecutor,
            preferencesStore = config.preferencesStore,
            fileStorage = config.fileStorage,
            logger = config.logger,
        ) + nodeDefinitions
    )
    val validator = ActionWorkflowValidator(registry)
    val codec = ActionWorkflowCodec(config.json, validator, registry)
    val engine = ActionWorkflowEngine(
        registry = registry,
        validator = validator,
        now = { kotlin.time.Clock.System.now().toEpochMilliseconds() },
    )
    return WorkflowRuntime(registry, validator, codec, engine, config)
}
