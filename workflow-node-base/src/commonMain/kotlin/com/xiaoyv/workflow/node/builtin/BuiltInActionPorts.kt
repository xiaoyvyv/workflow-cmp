package com.xiaoyv.workflow.node.builtin

import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.execution.ActionExecutionContext
import com.xiaoyv.workflow.model.execution.ActionNodeExecutionResult
import com.xiaoyv.workflow.model.spec.ActionArrayConfigKey
import com.xiaoyv.workflow.model.spec.ActionControlPortId
import com.xiaoyv.workflow.node.core.ActionPortConnectionLimit
import com.xiaoyv.workflow.node.core.ActionPortDirection
import com.xiaoyv.workflow.node.core.ActionPortSpec
import com.xiaoyv.workflow.node.core.string
import com.xiaoyv.workflow.node.resolver.ActionTemplateResolver
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

val inPort = ActionPortSpec(id = ActionControlPortId.IN, direction = ActionPortDirection.INPUT)
val nextPort = ActionPortSpec(id = ActionControlPortId.NEXT, direction = ActionPortDirection.OUTPUT)
val successPort = ActionPortSpec(id = ActionControlPortId.SUCCESS, direction = ActionPortDirection.OUTPUT)
val failurePort = ActionPortSpec(id = ActionControlPortId.FAILURE, direction = ActionPortDirection.OUTPUT)
val bodyPort = ActionPortSpec(id = ActionControlPortId.BODY, direction = ActionPortDirection.OUTPUT)
val completedPort = ActionPortSpec(id = ActionControlPortId.COMPLETED, direction = ActionPortDirection.OUTPUT)
val loopControlInPort = ActionPortSpec(id = ActionControlPortId.IN, direction = ActionPortDirection.INPUT, maxConnections = ActionPortConnectionLimit.UNLIMITED)
val matchedPort = ActionPortSpec(id = ActionControlPortId.MATCHED, direction = ActionPortDirection.OUTPUT)
val defaultPort = ActionPortSpec(id = ActionControlPortId.DEFAULT, direction = ActionPortDirection.OUTPUT)
val tryPort = ActionPortSpec(id = ActionControlPortId.TRY, direction = ActionPortDirection.OUTPUT)
val catchPort = ActionPortSpec(id = ActionControlPortId.CATCH, direction = ActionPortDirection.OUTPUT)
val finallyPort = ActionPortSpec(id = ActionControlPortId.FINALLY, direction = ActionPortDirection.OUTPUT)
val branchesPort = ActionPortSpec(id = ActionControlPortId.BRANCHES, direction = ActionPortDirection.OUTPUT)
val truePort = ActionPortSpec(id = ActionControlPortId.TRUE, direction = ActionPortDirection.OUTPUT)
val falsePort = ActionPortSpec(id = ActionControlPortId.FALSE, direction = ActionPortDirection.OUTPUT)
val conditionPorts = persistentListOf(truePort, falsePort)

fun ActionNode.valueResult(key: String, value: JsonElement): ActionNodeExecutionResult {
    require(key.isNotBlank()) { "outputKey 不能为空" }
    return ActionNodeExecutionResult(outputPortId = "next", output = JsonObject(mapOf(key to value)))
}

fun ActionNode.values(context: ActionExecutionContext): JsonArray =
    ActionTemplateResolver.resolveElement(config[ActionArrayConfigKey.VALUES], context) as? JsonArray
        ?: error("数组节点 values 必须是数组")

fun ActionNode.arrayResult(value: JsonArray) = valueResult(config.string(ActionArrayConfigKey.OUTPUT_KEY), value)
