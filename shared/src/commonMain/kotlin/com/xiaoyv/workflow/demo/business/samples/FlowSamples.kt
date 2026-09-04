package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionControlConfigKey
import com.xiaoyv.workflow.model.spec.ActionControlPortId
import com.xiaoyv.workflow.model.spec.ActionDataConfigKey
import com.xiaoyv.workflow.model.spec.ActionFlowConfigKey
import com.xiaoyv.workflow.model.spec.ActionLoopConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType
import com.xiaoyv.workflow.model.spec.ActionToastConfigKey
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * 流程流转、条件分支、循环控制与并发合流测试工作流样例集合（workflow-node-control）。
 */
internal object FlowSamples {
    val all: List<ActionWorkflow> = listOf(
        // 1. Flow 流程基础控制
        linear("flow_start_end", "流程开始与结束", ActionNodeType.FLOW_END),
        linear("flow_delay", "延迟等待 (3000ms)", ActionNodeType.FLOW_DELAY, config(ActionFlowConfigKey.DELAY_MILLIS to 3000L)),
        linear("flow_log", "控制台输出日志", ActionNodeType.FLOW_LOG, config(ActionFlowConfigKey.MESSAGE to "测试日志信息")),
        linear("flow_debug", "流程调试断点", ActionNodeType.FLOW_DEBUG, config(ActionFlowConfigKey.MESSAGE to "到达调试节点")),
        linear("flow_assert", "流程前置断言", ActionNodeType.FLOW_ASSERT, config(ActionFlowConfigKey.CONDITION to true)),
        linear("flow_rate_limit", "流程速率限制", ActionNodeType.FLOW_RATE_LIMIT, config(ActionFlowConfigKey.DELAY_MILLIS to 500L)),
        terminal("flow_stop", "提前终止工作流", ActionNodeType.FLOW_STOP),
        switchSample(),
        tryCatchFinallySample(),
        forkAndJoinSample(),
        parallelTimingSample(),

        // 2. Control 条件分支与比较
        condition("control_if", "条件分支 (If-Else)", ActionNodeType.CONDITION_IF, config(ActionControlConfigKey.CONDITION to true)),
        condition("control_equals", "相等判断 (Equals)", ActionNodeType.CONDITION_EQUALS, config(ActionControlConfigKey.LEFT to 7, ActionControlConfigKey.RIGHT to 7)),
        condition(
            "control_not_equals",
            "不相等判断 (Not Equals)",
            ActionNodeType.CONDITION_NOT_EQUALS,
            config(ActionControlConfigKey.LEFT to "A", ActionControlConfigKey.RIGHT to "B")
        ),
        condition(
            "control_greater_than",
            "大于判断 (Greater Than)",
            ActionNodeType.CONDITION_GREATER_THAN,
            config(ActionControlConfigKey.LEFT to 8, ActionControlConfigKey.RIGHT to 7)
        ),
        condition(
            "control_greater_than_or_equals",
            "大于等于判断 (GTE)",
            ActionNodeType.CONDITION_GREATER_THAN_OR_EQUALS,
            config(ActionControlConfigKey.LEFT to 7, ActionControlConfigKey.RIGHT to 7),
        ),
        condition("control_less_than", "小于判断 (Less Than)", ActionNodeType.CONDITION_LESS_THAN, config(ActionControlConfigKey.LEFT to 6, ActionControlConfigKey.RIGHT to 7)),
        condition(
            "control_less_than_or_equals",
            "小于等于判断 (LTE)",
            ActionNodeType.CONDITION_LESS_THAN_OR_EQUALS,
            config(ActionControlConfigKey.LEFT to 7, ActionControlConfigKey.RIGHT to 7),
        ),
        condition("control_and", "逻辑与 (And)", ActionNodeType.CONDITION_AND, config(ActionControlConfigKey.LEFT to true, ActionControlConfigKey.RIGHT to true)),
        condition("control_or", "逻辑或 (Or)", ActionNodeType.CONDITION_OR, config(ActionControlConfigKey.LEFT to false, ActionControlConfigKey.RIGHT to true)),
        condition("control_not", "逻辑非 (Not)", ActionNodeType.CONDITION_NOT, config(ActionControlConfigKey.VALUE to false)),
        condition("control_is_null", "空值判断 (Is Null)", ActionNodeType.CONDITION_IS_NULL, buildJsonObject { put(ActionControlConfigKey.VALUE, JsonNull) }),
        condition("control_is_empty", "空串/空集合判定 (Is Empty)", ActionNodeType.CONDITION_IS_EMPTY, config(ActionControlConfigKey.VALUE to "")),
        condition("control_http_status", "HTTP 状态码判定 (Http Status)", ActionNodeType.HTTP_STATUS, config(ActionControlConfigKey.STATUS_CODE to 200)),

        // 3. Loop 循环控制
        loopRepeatSample(),
        loopForEachSample(),
        loopWhileSample(),
    )

    private fun switchSample(): ActionWorkflow = workflow(
        id = "flow_switch",
        name = "多分支选择 (Switch)",
        description = "匹配目标值在案例表中时从 matched 端口继续。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "target",
                ActionNodeType.FLOW_SWITCH,
                "匹配状态",
                buildJsonObject {
                    put(ActionFlowConfigKey.VALUE, JsonPrimitive("published"))
                    put(ActionFlowConfigKey.CASES, JsonObject(mapOf("published" to JsonPrimitive(true), "airing" to JsonPrimitive(true))))
                },
            ),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(edge("start", ActionControlPortId.NEXT, "target"), edge("target", ActionControlPortId.MATCHED, "end")),
    )

    private fun tryCatchFinallySample(): ActionWorkflow = workflow(
        id = "flow_try_catch_finally",
        name = "异常捕获 (Try-Catch-Finally)",
        description = "演示在 Try 块中发生异常时自动路由至 Catch 分支，并最终统一执行 Finally 块。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node("try", ActionNodeType.FLOW_TRY, "尝试执行"),
            node("catch", ActionNodeType.FLOW_CATCH, "异常捕获", config(ActionFlowConfigKey.OUTPUT_KEY to "capturedError")),
            node("finally", ActionNodeType.FLOW_FINALLY, "最终执行"),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "try"),
            edge("try", ActionControlPortId.TRY, "finally"),
            edge("try", ActionControlPortId.CATCH, "catch"),
            edge("catch", ActionControlPortId.NEXT, "finally"),
            edge("finally", ActionControlPortId.NEXT, "end"),
        ),
    )

    private fun forkAndJoinSample(): ActionWorkflow = workflow(
        id = "flow_fork_join",
        name = "多路分叉与合流 (Fork-Join)",
        description = "从单个入口节点分叉出两条并行分支（左路与右路），各自计算变量后再合流汇入 Join 节点合并数据。",
        capabilities = emptySet(),
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "流程开始"),
            node("node_left", ActionNodeType.SET_VARIABLE, "左路分支", config(ActionDataConfigKey.KEY to "left_val", ActionDataConfigKey.VALUE to "左路数据")),
            node("node_right", ActionNodeType.SET_VARIABLE, "右路分支", config(ActionDataConfigKey.KEY to "right_val", ActionDataConfigKey.VALUE to "右路数据")),
            node(
                "node_join",
                ActionNodeType.TEMPLATE,
                "多路合流",
                config(
                    ActionDataConfigKey.TEMPLATE to "合流结果: \${steps.node_left.left_val} + \${steps.node_right.right_val}",
                    ActionDataConfigKey.OUTPUT_KEY to "result",
                ),
            ),
            node("toast", ActionNodeType.SHOW_TOAST, "弹出结果", config(ActionToastConfigKey.MESSAGE to "\${steps.node_join.result}")),
            node("end", ActionNodeType.FLOW_END, "流程结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "node_left"),
            edge("start", ActionControlPortId.NEXT, "node_right"),
            edge("node_left", ActionControlPortId.NEXT, "node_join"),
            edge("node_right", ActionControlPortId.NEXT, "node_join"),
            edge("node_join", ActionControlPortId.NEXT, "toast"),
            edge("toast", ActionControlPortId.SUCCESS, "end"),
        ),
    )

    private fun parallelTimingSample(): ActionWorkflow = workflow(
        id = "flow_parallel_timing",
        name = "并发分支耗时验证 (Parallel)",
        description = "两条分支分别等待 1 秒与 3 秒并在 flow.join 汇合，总耗时接近 3 秒而非 4 秒。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node("parallel", ActionNodeType.FLOW_PARALLEL, "并发分支"),
            node("short_delay", ActionNodeType.FLOW_DELAY, "短任务 (1s)", config(ActionFlowConfigKey.DELAY_MILLIS to 1_000L)),
            node("long_delay", ActionNodeType.FLOW_DELAY, "长任务 (3s)", config(ActionFlowConfigKey.DELAY_MILLIS to 3_000L)),
            node(
                "join",
                ActionNodeType.FLOW_JOIN,
                "等待全部完成",
                config(ActionFlowConfigKey.VALUES to JsonArray(emptyList()), ActionFlowConfigKey.OUTPUT_KEY to "parallelResult"),
            ),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "parallel"),
            edge("parallel", ActionControlPortId.BRANCHES, "short_delay"),
            edge("parallel", ActionControlPortId.BRANCHES, "long_delay"),
            edge("short_delay", ActionControlPortId.NEXT, "join"),
            edge("long_delay", ActionControlPortId.NEXT, "join"),
            edge("join", ActionControlPortId.NEXT, "end"),
        ),
    )

    private fun loopRepeatSample(): ActionWorkflow = workflow(
        id = "loop_repeat_count",
        name = "固定次数循环 (Loop Repeat)",
        description = "演示通过 loop.repeat 循环 5 次并用 loop.next 流转。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node("init_count", ActionNodeType.SET_VARIABLE, "初始化计数", config(ActionDataConfigKey.KEY to "count", ActionDataConfigKey.VALUE to 0)),
            node("loop", ActionNodeType.LOOP_REPEAT, "循环 5 次", config(ActionLoopConfigKey.COUNT to 5, ActionLoopConfigKey.MAX_ITERATIONS to 10)),
            node("add_count", ActionNodeType.SET_VARIABLE, "计数加 1", config(ActionDataConfigKey.KEY to "count", ActionDataConfigKey.VALUE to "\${vars.count + 1}")),
            node("next", ActionNodeType.LOOP_NEXT, "下一轮", config(ActionLoopConfigKey.LOOP_ID to "loop")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "init_count"),
            edge("init_count", ActionControlPortId.NEXT, "loop"),
            edge("loop", ActionControlPortId.BODY, "add_count"),
            edge("add_count", ActionControlPortId.NEXT, "next"),
            edge("loop", ActionControlPortId.COMPLETED, "end"),
        ),
    )

    private fun loopForEachSample(): ActionWorkflow = workflow(
        id = "loop_for_each_items",
        name = "集合遍历循环 (Loop For Each)",
        description = "演示通过 loop.for_each 逐项遍历数组元素。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "loop",
                ActionNodeType.LOOP_FOR_EACH,
                "遍历水果列表",
                buildJsonObject {
                    put(ActionLoopConfigKey.ITEMS, buildJsonArray {
                        add(JsonPrimitive("Apple"))
                        add(JsonPrimitive("Banana"))
                        add(JsonPrimitive("Cherry"))
                    })
                },
            ),
            node("log_item", ActionNodeType.FLOW_LOG, "打印当前项", config(ActionFlowConfigKey.MESSAGE to "循环当前项: \${loop.item}")),
            node("next", ActionNodeType.LOOP_NEXT, "下一项", config(ActionLoopConfigKey.LOOP_ID to "loop")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "loop"),
            edge("loop", ActionControlPortId.BODY, "log_item"),
            edge("log_item", ActionControlPortId.NEXT, "next"),
            edge("loop", ActionControlPortId.COMPLETED, "end"),
        ),
    )

    private fun loopWhileSample(): ActionWorkflow = workflow(
        id = "loop_while_condition",
        name = "条件循环 (Loop While)",
        description = "演示在条件满足时持续循环并在满足特定条件时 break 退出。",
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node("init_val", ActionNodeType.SET_VARIABLE, "初始化变量", config(ActionDataConfigKey.KEY to "n", ActionDataConfigKey.VALUE to 0)),
            node("loop", ActionNodeType.LOOP_WHILE, "条件循环", config(ActionLoopConfigKey.CONDITION to true, ActionLoopConfigKey.MAX_ITERATIONS to 5)),
            node("inc", ActionNodeType.SET_VARIABLE, "自增", config(ActionDataConfigKey.KEY to "n", ActionDataConfigKey.VALUE to "\${vars.n + 1}")),
            node("next", ActionNodeType.LOOP_NEXT, "继续", config(ActionLoopConfigKey.LOOP_ID to "loop")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "init_val"),
            edge("init_val", ActionControlPortId.NEXT, "loop"),
            edge("loop", ActionControlPortId.BODY, "inc"),
            edge("inc", ActionControlPortId.NEXT, "next"),
            edge("loop", ActionControlPortId.COMPLETED, "end"),
        ),
    )
}
