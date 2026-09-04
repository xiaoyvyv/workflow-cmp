package com.xiaoyv.workflow.engine

import com.xiaoyv.workflow.exception.ActionValidationCode
import com.xiaoyv.workflow.model.definition.ActionEdge
import com.xiaoyv.workflow.model.definition.ActionNode
import com.xiaoyv.workflow.model.definition.ActionPortRef
import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionControlPortId
import com.xiaoyv.workflow.model.spec.ActionFlowConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType
import com.xiaoyv.workflow.node.builtin.builtInActionNodeDefinitions
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import com.xiaoyv.workflow.port.ActionWorkflowPreferencesStore
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * flow.parallel 与 flow.join 的静态拓扑校验测试。
 */
class ActionWorkflowParallelValidationTest {
    private val validator = ActionWorkflowValidator(
        ActionNodeRegistry(
            builtInActionNodeDefinitions(
                ActionHttpRequestExecutor { buildJsonObject {} },
                object : ActionWorkflowPreferencesStore {
                    override suspend fun get(key: String): JsonElement? = null
                    override suspend fun set(key: String, value: JsonElement) = Unit
                    override suspend fun delete(key: String) = Unit
                    override suspend fun has(key: String) = false
                    override suspend fun clear() = Unit
                },
            )
        ),
    )

    @Test
    fun detectsMissingBranchesAndStandaloneJoin() {
        val workflow = workflow(
            nodes = listOf(node("start", ActionNodeType.FLOW_START), node("parallel", ActionNodeType.FLOW_PARALLEL), join("join")),
            edges = listOf(edge("start", ActionControlPortId.NEXT, "parallel"))
        )
        val codes = validator.validate(workflow).issues.map { it.code }.toSet()
        assertTrue(ActionValidationCode.PARALLEL_MISSING_BRANCH in codes)
        assertTrue(ActionValidationCode.JOIN_NOT_PAIRED in codes)
    }

    @Test
    fun detectsBranchesWithoutCommonJoin() {
        val workflow = workflow(
            nodes = listOf(
                node("start", ActionNodeType.FLOW_START),
                node("parallel", ActionNodeType.FLOW_PARALLEL),
                node("left", ActionNodeType.FLOW_END),
                node("right", ActionNodeType.FLOW_END)
            ),
            edges = listOf(
                edge("start", ActionControlPortId.NEXT, "parallel"),
                edge("parallel", ActionControlPortId.BRANCHES, "left"),
                edge("parallel", ActionControlPortId.BRANCHES, "right")
            ),
        )
        assertTrue(validator.validate(workflow).issues.any { it.code == ActionValidationCode.PARALLEL_JOIN_MISSING && it.nodeId == "parallel" })
    }

    @Test
    fun detectsNestedParallel() {
        val workflow = workflow(
            nodes = listOf(
                node("start", ActionNodeType.FLOW_START),
                node("outer", ActionNodeType.FLOW_PARALLEL),
                node("inner", ActionNodeType.FLOW_PARALLEL),
                node("other", ActionNodeType.FLOW_DELAY, config = buildJsonObject { put(ActionFlowConfigKey.DELAY_MILLIS, JsonPrimitive(0)) }),
                join("join")
            ),
            edges = listOf(
                edge("start", ActionControlPortId.NEXT, "outer"),
                edge("outer", ActionControlPortId.BRANCHES, "inner"),
                edge("outer", ActionControlPortId.BRANCHES, "other"),
                edge("inner", ActionControlPortId.BRANCHES, "join"),
                edge("other", ActionControlPortId.NEXT, "join")
            ),
        )
        assertTrue(validator.validate(workflow).issues.any { it.code == ActionValidationCode.PARALLEL_NESTED && it.nodeId == "outer" })
    }

    private fun workflow(nodes: List<ActionNode>, edges: List<ActionEdge>) =
        ActionWorkflow(id = "parallel_validation", name = "parallel", entryNodeId = "start", nodes = nodes.toPersistentList(), edges = edges.toPersistentList())

    private fun node(id: String, type: String, config: kotlinx.serialization.json.JsonObject = buildJsonObject {}) = ActionNode(id = id, type = type, config = config)
    private fun join(id: String) = node(
        id,
        ActionNodeType.FLOW_JOIN,
        buildJsonObject { put(ActionFlowConfigKey.VALUES, kotlinx.serialization.json.JsonArray(emptyList())); put(ActionFlowConfigKey.OUTPUT_KEY, JsonPrimitive("value")) })

    private fun edge(from: String, port: String, to: String) = ActionEdge("$from-$port-$to", source = ActionPortRef(from, port), target = ActionPortRef(to, ActionControlPortId.IN))
}
