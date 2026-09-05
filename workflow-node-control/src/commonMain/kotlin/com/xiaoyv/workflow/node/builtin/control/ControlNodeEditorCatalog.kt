package com.xiaoyv.workflow.node.builtin.control

import com.xiaoyv.workflow.model.spec.ActionControlConfigKey
import com.xiaoyv.workflow.model.spec.ActionFlowConfigKey
import com.xiaoyv.workflow.model.spec.ActionFlowLogLevel
import com.xiaoyv.workflow.model.spec.ActionLoopConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * 控制流、分支条件与循环模块的集中编辑器说明目录。
 */
internal object ControlNodeEditorCatalog {
    // ==================== 流程控制 (Flow) ====================

    val start =
        ActionNodeEditorSpec(
            title = "开始",
            description = "工作流的唯一入口节点。",
            icon = "play",
            color = "#22C55E",
            creatable = false,
        )

    val flowSwitch =
        ActionNodeEditorSpec(
            title = "多路分支 (Switch)",
            description = "根据匹配值与分支映射表分发到 matched 或 default 端口。",
            icon = "git-branch",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.VALUE,
                        label = "匹配值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "用于匹配分支的值",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.CASES,
                        label = "分支映射 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "{\"value1\": true, \"value2\": true}",
                        order = 1,
                    ),
                ),
        )

    val end =
        ActionNodeEditorSpec(
            title = "结束",
            description = "结束当前工作流执行。",
            icon = "stop-circle",
            color = "#EF4444",
        )

    val delay =
        ActionNodeEditorSpec(
            title = "延时等待",
            description = "暂停当前分支指定的时间（毫秒）。",
            icon = "timer",
            color = "#6366F1",
            defaultConfig = buildJsonObject { put(ActionFlowConfigKey.DELAY_MILLIS, JsonPrimitive(1_000)) },
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.DELAY_MILLIS,
                        label = "延时时间（毫秒）",
                        kind = ActionEditorFieldKind.NUMBER,
                        defaultValue = JsonPrimitive(1_000),
                        required = true,
                        order = 0,
                    ),
                ),
        )

    val flowLog =
        ActionNodeEditorSpec(
            title = "记录日志",
            description = "在工作流执行期间输出日志消息与附加数据。",
            icon = "file-text",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.MESSAGE,
                        label = "日志消息",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "要输出的日志内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.LEVEL,
                        label = "日志级别",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionFlowLogLevel.INFO),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionFlowLogLevel.DEBUG), "DEBUG"),
                                ActionEditorFieldOption(JsonPrimitive(ActionFlowLogLevel.INFO), "INFO"),
                                ActionEditorFieldOption(JsonPrimitive(ActionFlowLogLevel.WARN), "WARN"),
                                ActionEditorFieldOption(JsonPrimitive(ActionFlowLogLevel.ERROR), "ERROR"),
                            ),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.DATA,
                        label = "附加数据 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "可选的附加调试/上下文数据",
                        order = 2,
                    ),
                ),
        )

    val flowAssert =
        ActionNodeEditorSpec(
            title = "断言检查",
            description = "校验条件表达式，若为假则抛出异常中断流程。",
            icon = "check-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.CONDITION,
                        label = "断言条件表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：{{status}} == 200",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.MESSAGE,
                        label = "失败提示信息",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "断言失败时抛出的错误提示",
                        order = 1,
                    ),
                ),
        )

    val stop =
        ActionNodeEditorSpec(
            title = "停止执行",
            description = "立即停止当前分支的后续执行。",
            icon = "square",
            color = "#F59E0B",
        )

    val flowDebug =
        ActionNodeEditorSpec(
            title = "调试断点",
            description = "打印调试信息并导出当前变量快照。",
            icon = "bug",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.MESSAGE,
                        label = "调试信息",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "可选的调试提示",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.DATA,
                        label = "调试数据 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 1,
                    ),
                ),
        )

    val flowTry =
        ActionNodeEditorSpec(
            title = "异常尝试 (Try)",
            description = "开启受保护的分支执行，发生错误时捕获并流向 Catch 分支。",
            icon = "shield",
        )

    val flowCatch =
        ActionNodeEditorSpec(
            title = "异常捕获 (Catch)",
            description = "接收并处理前序 Try 块中产生的异常错误。",
            icon = "alert-triangle",
        )

    val flowFinally =
        ActionNodeEditorSpec(
            title = "最终执行 (Finally)",
            description = "无论是否产生异常，Try-Catch 块结束前必定执行的分支。",
            icon = "anchor",
        )

    val flowCall =
        ActionNodeEditorSpec(
            title = "调用工作流",
            description = "调用并执行指定的子工作流并接收其输出数据。",
            icon = "external-link",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.WORKFLOW_ID,
                        label = "子工作流 ID",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "目标子工作流的唯一标识",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.INPUT,
                        label = "输入数据 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "传递给子工作流的输入数据",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("subWorkflowResult"),
                        order = 2,
                    ),
                ),
        )

    val returnNode =
        ActionNodeEditorSpec(
            title = "返回",
            description = "从被调用的子工作流返回结果数据。",
            icon = "corner-up-left",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.OUTPUT,
                        label = "返回结果数据 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 0,
                    ),
                ),
        )

    val parallel =
        ActionNodeEditorSpec(
            title = "并行分支",
            description = "同时触发并并行执行多个分支流程。",
            icon = "git-pull-request",
            color = "#3B82F6",
        )

    val join =
        ActionNodeEditorSpec(
            title = "合流",
            description = "等待并汇集多个并行分支的输出数据。",
            icon = "git-merge",
            color = "#3B82F6",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.VALUES,
                        label = "待汇集的值列表",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "例如：[{{branch1.data}}, {{branch2.data}}]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("joined"),
                        order = 1,
                    ),
                ),
        )

    val flowRetry =
        ActionNodeEditorSpec(
            title = "失败重试",
            description = "配置当前分支在后续节点失败时的重试次数与间隔。",
            icon = "refresh-cw",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.RETRY_COUNT,
                        label = "最大重试次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(3),
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.RETRY_DELAY_MILLIS,
                        label = "重试间隔（毫秒）",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(1_000),
                        order = 1,
                    ),
                ),
        )

    val flowTimeout =
        ActionNodeEditorSpec(
            title = "超时控制",
            description = "为后续流程设置最大执行超时时间，超时则走 failure 端口。",
            icon = "clock",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.TIMEOUT_MILLIS,
                        label = "超时时间（毫秒）",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(5_000),
                        order = 0,
                    ),
                ),
        )

    val flowWaitUntil =
        ActionNodeEditorSpec(
            title = "等待直到",
            description = "检查条件表达式是否成立，成立走 next，不成立走 failure。",
            icon = "pause-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.CONDITION,
                        label = "等待条件表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：{{status}} == 'READY'",
                        order = 0,
                    ),
                ),
        )

    val flowRateLimit =
        ActionNodeEditorSpec(
            title = "速率限制",
            description = "限制流程执行频率，自动延时控制吞吐。",
            icon = "sliders",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFlowConfigKey.DELAY_MILLIS,
                        label = "限速间隔（毫秒）",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(1_000),
                        order = 0,
                    ),
                ),
        )

    // ==================== 条件与比较 (Control) ====================

    val conditionIsEmpty =
        ActionNodeEditorSpec(
            title = "判断为空",
            description = "判断变量或输入值是否为 null、空字符串或空对象。",
            icon = "help-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.VALUE,
                        label = "待检查的值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "输入待检查的变量或内容",
                        order = 0,
                    ),
                ),
        )

    val conditionIf =
        ActionNodeEditorSpec(
            title = "条件判断 (If)",
            description = "评估布尔条件表达式，为真走 true 端口，为假走 false 端口。",
            icon = "git-branch",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.CONDITION,
                        label = "条件表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：{{count}} > 0",
                        order = 0,
                    ),
                ),
        )

    val httpStatus =
        ActionNodeEditorSpec(
            title = "HTTP 状态码判断",
            description = "判断 HTTP 响应状态码是否在预期成功区间（默认 200~299）。",
            icon = "activity",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.STATUS_CODE,
                        label = "HTTP 状态码",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        placeholder = "例如：200 或 {{response.statusCode}}",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.MIN_STATUS_CODE,
                        label = "最小状态码 (包含)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(200),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.MAX_STATUS_CODE,
                        label = "最大状态码 (包含)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(299),
                        order = 2,
                    ),
                ),
        )

    val conditionEquals =
        ActionNodeEditorSpec(
            title = "判断相等 (==)",
            description = "判断左值与右值是否相等。",
            icon = "equal",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionNotEquals =
        ActionNodeEditorSpec(
            title = "判断不相等 (!=)",
            description = "判断左值与右值是否不相等。",
            icon = "divide-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionGreaterThan =
        ActionNodeEditorSpec(
            title = "判断大于 (>)",
            description = "判断左值是否大于右值。",
            icon = "chevron-right",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionGreaterThanOrEquals =
        ActionNodeEditorSpec(
            title = "判断大于等于 (>=)",
            description = "判断左值是否大于或等于右值。",
            icon = "chevrons-right",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionLessThan =
        ActionNodeEditorSpec(
            title = "判断小于 (<)",
            description = "判断左值是否小于右值。",
            icon = "chevron-left",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionLessThanOrEquals =
        ActionNodeEditorSpec(
            title = "判断小于等于 (<=)",
            description = "判断左值是否小于或等于右值。",
            icon = "chevrons-left",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右操作数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionAnd =
        ActionNodeEditorSpec(
            title = "逻辑与 (AND)",
            description = "判断左值与右值是否同时为真。",
            icon = "link-2",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左条件",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右条件",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionOr =
        ActionNodeEditorSpec(
            title = "逻辑或 (OR)",
            description = "判断左值与右值是否至少有一个为真。",
            icon = "shuffle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.LEFT,
                        label = "左条件",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.RIGHT,
                        label = "右条件",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val conditionNot =
        ActionNodeEditorSpec(
            title = "逻辑非 (NOT)",
            description = "对输入条件或布尔值进行取反操作。",
            icon = "slash",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.VALUE,
                        label = "待取反的值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                ),
        )

    val conditionIsNull =
        ActionNodeEditorSpec(
            title = "判断为 Null",
            description = "判断给定的变量或值是否为 null。",
            icon = "circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionControlConfigKey.VALUE,
                        label = "待检查的值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                ),
        )

    // ==================== 循环控制 (Loop) ====================

    val loopRepeat =
        ActionNodeEditorSpec(
            title = "次数循环 (Repeat)",
            description = "按指定次数重复执行循环体分支流程。",
            icon = "repeat",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.COUNT,
                        label = "循环次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(5),
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.MAX_ITERATIONS,
                        label = "最大安全迭代次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(10_000),
                        order = 1,
                    ),
                ),
        )

    val loopForEach =
        ActionNodeEditorSpec(
            title = "遍历循环 (ForEach)",
            description = "遍历数组或列表中的每一个元素并执行循环体分支。",
            icon = "list",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.ITEMS,
                        label = "待遍历的数组/列表",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "例如：{{items}} 或 [1, 2, 3]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.MAX_ITERATIONS,
                        label = "最大安全迭代次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(10_000),
                        order = 1,
                    ),
                ),
        )

    val loopWhile =
        ActionNodeEditorSpec(
            title = "条件循环 (While)",
            description = "当条件表达式为真时持续重复执行循环体分支。",
            icon = "rotate-cw",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.CONDITION,
                        label = "继续循环条件表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：{{index}} < 10",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.MAX_ITERATIONS,
                        label = "最大安全迭代次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(10_000),
                        order = 1,
                    ),
                ),
        )

    val loopNext =
        ActionNodeEditorSpec(
            title = "循环步进 (Next)",
            description = "标记当前循环轮次完成，进入下一轮迭代。",
            icon = "arrow-right-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.LOOP_ID,
                        label = "关联循环节点 ID",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "可选：多层嵌套循环时指定循环 ID",
                        order = 0,
                    ),
                ),
        )

    val loopContinue =
        ActionNodeEditorSpec(
            title = "跳过本轮 (Continue)",
            description = "跳过当前循环的后续步骤，直接进入下一轮迭代。",
            icon = "skip-forward",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.LOOP_ID,
                        label = "关联循环节点 ID",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "可选：多层嵌套循环时指定循环 ID",
                        order = 0,
                    ),
                ),
        )

    val loopBreak =
        ActionNodeEditorSpec(
            title = "中断循环 (Break)",
            description = "立即中断并跳出循环，流向 completed 端口。",
            icon = "x-circle",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionLoopConfigKey.LOOP_ID,
                        label = "关联循环节点 ID",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "可选：多层嵌套循环时指定循环 ID",
                        order = 0,
                    ),
                ),
        )
}
