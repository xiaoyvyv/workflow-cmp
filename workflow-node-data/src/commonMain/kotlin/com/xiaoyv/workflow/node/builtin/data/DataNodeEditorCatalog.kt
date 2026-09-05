package com.xiaoyv.workflow.node.builtin.data

import com.xiaoyv.workflow.model.spec.ActionArrayConfigKey
import com.xiaoyv.workflow.model.spec.ActionArrayFilterOperator
import com.xiaoyv.workflow.model.spec.ActionDataConfigKey
import com.xiaoyv.workflow.model.spec.ActionDataMergeStrategy
import com.xiaoyv.workflow.model.spec.ActionMathConfigKey
import com.xiaoyv.workflow.model.spec.ActionObjectConfigKey
import com.xiaoyv.workflow.model.spec.ActionTextConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

/**
 * 数据、文本、数组、对象和数学节点的集中编辑器说明目录。
 */
internal object DataNodeEditorCatalog {
    // ==================== 基础数据处理 (Data) ====================

    val template =
        ActionNodeEditorSpec(
            title = "模板字符串渲染",
            description = "解析并渲染包含 {{vars.name}} 变量插值的模板文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.TEMPLATE,
                        label = "模板内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：你好，{{name}}！",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("renderedText"),
                        order = 1,
                    ),
                ),
        )

    val setVariable =
        ActionNodeEditorSpec(
            title = "设置变量",
            description = "将值写入工作流上下文变量中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.KEY,
                        label = "变量名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：userName",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUE,
                        label = "变量值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "输入静态值或变量表达式",
                        order = 1,
                    ),
                ),
        )

    val coalesce =
        ActionNodeEditorSpec(
            title = "空值合并 (Coalesce)",
            description = "按顺序返回第一个非 null 且非空的有效值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUES,
                        label = "候选值列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[{{val1}}, {{val2}}, \"默认值\"]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("coalesced"),
                        order = 1,
                    ),
                ),
        )

    val concat =
        ActionNodeEditorSpec(
            title = "数据拼接",
            description = "将多个值或字符串列表拼接为一个字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUES,
                        label = "待拼接值列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[{{prefix}}, \"-\", {{suffix}}]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("concatenated"),
                        order = 1,
                    ),
                ),
        )

    val dataMerge =
        ActionNodeEditorSpec(
            title = "深度合并对象",
            description = "将对象列表中的多个 JSON 对象按合并策略进行合并。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OBJECTS,
                        label = "待合并对象列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.MERGE_STRATEGY,
                        label = "合并策略",
                        kind = ActionEditorFieldKind.SELECT,
                        required = true,
                        defaultValue = JsonPrimitive(ActionDataMergeStrategy.DEEP),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionDataMergeStrategy.SHALLOW), "浅度合并 (顶层覆盖)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDataMergeStrategy.DEEP), "深度合并 (递归合并对象)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDataMergeStrategy.DEEP_APPEND_ARRAYS), "深度合并并追加数组"),
                            ),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("mergedObject"),
                        order = 2,
                    ),
                ),
        )

    val dataAssign =
        ActionNodeEditorSpec(
            title = "对象字段赋值",
            description = "在目标 JSON 对象上批量设置指定路径的值并输出新对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.ASSIGNMENTS,
                        label = "字段赋值映射 (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "{\"name\": \"张三\", \"$.profile.age\": 18}",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("assignedObject"),
                        order = 2,
                    ),
                ),
        )

    val dataRemove =
        ActionNodeEditorSpec(
            title = "对象字段移除",
            description = "从目标 JSON 对象中移除指定路径的属性字段。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.PATH,
                        label = "要移除的字段路径 (JSONPath)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.profile.tempKey",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("reducedObject"),
                        order = 2,
                    ),
                ),
        )

    val dataRename =
        ActionNodeEditorSpec(
            title = "对象字段重命名",
            description = "将目标 JSON 对象中的指定路径键名移动/更改为新路径。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.FROM_PATH,
                        label = "原字段路径 (JSONPath)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.oldKey",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.TO_PATH,
                        label = "新字段路径 (JSONPath)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.newKey",
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("renamedObject"),
                        order = 3,
                    ),
                ),
        )

    val dataPick =
        ActionNodeEditorSpec(
            title = "选取指定字段",
            description = "从目标 JSON 对象中按 JSONPath 路径列表挑出指定字段构成新对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.PATHS,
                        label = "要提取的路径列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[\"$.id\", \"$.profile.name\"]",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("pickedObject"),
                        order = 2,
                    ),
                ),
        )

    val dataUuid =
        ActionNodeEditorSpec(
            title = "生成随机 UUID",
            description = "生成一个标准的 UUID v4 唯一标识字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("uuid"),
                        order = 0,
                    ),
                ),
        )

    val dataToNumber =
        ActionNodeEditorSpec(
            title = "转换为数字 (ToNumber)",
            description = "将字符串或其它数据类型强制转换为数值类型。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUE,
                        label = "待转换的值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("numberValue"),
                        order = 1,
                    ),
                ),
        )

    val dataToString =
        ActionNodeEditorSpec(
            title = "转换为文本 (ToString)",
            description = "将数值、布尔或对象数据转换为字符串文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUE,
                        label = "待转换的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("stringValue"),
                        order = 1,
                    ),
                ),
        )

    val dataToBoolean =
        ActionNodeEditorSpec(
            title = "转换为布尔 (ToBoolean)",
            description = "将输入数据转换为真/假布尔值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUE,
                        label = "待转换的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("booleanValue"),
                        order = 1,
                    ),
                ),
        )

    val dataTypeOf =
        ActionNodeEditorSpec(
            title = "获取数据类型 (TypeOf)",
            description = "获取目标变量或值的数据类型名称（string, number, boolean, array, object, null）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.VALUE,
                        label = "待检查的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDataConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("type"),
                        order = 1,
                    ),
                ),
        )

    // ==================== 数学运算 (Math) ====================

    val mathAdd =
        ActionNodeEditorSpec(
            title = "加法运算 (+)",
            description = "计算左值与右值的和（left + right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "加数 A",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "加数 B",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sum"),
                        order = 2,
                    ),
                ),
        )

    val mathSubtract =
        ActionNodeEditorSpec(
            title = "减法运算 (-)",
            description = "计算左值减去右值的差（left - right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "被减数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "减数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("difference"),
                        order = 2,
                    ),
                ),
        )

    val mathMultiply =
        ActionNodeEditorSpec(
            title = "乘法运算 (*)",
            description = "计算左值与右值的乘积（left * right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "乘数 A",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "乘数 B",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("product"),
                        order = 2,
                    ),
                ),
        )

    val mathDivide =
        ActionNodeEditorSpec(
            title = "除法运算 (/)",
            description = "计算左值除以右值的商（left / right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "被除数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "除数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("quotient"),
                        order = 2,
                    ),
                ),
        )

    val mathModulo =
        ActionNodeEditorSpec(
            title = "求余运算 (%)",
            description = "计算左值除以右值的余数（left % right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "被除数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "除数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("remainder"),
                        order = 2,
                    ),
                ),
        )

    val mathMin =
        ActionNodeEditorSpec(
            title = "求较小值 (Min)",
            description = "计算左值与右值中的较小者。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "数值 A",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "数值 B",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("minValue"),
                        order = 2,
                    ),
                ),
        )

    val mathMax =
        ActionNodeEditorSpec(
            title = "求较大值 (Max)",
            description = "计算左值与右值中的较大者。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "数值 A",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "数值 B",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("maxValue"),
                        order = 2,
                    ),
                ),
        )

    val mathPow =
        ActionNodeEditorSpec(
            title = "幂运算 (Power)",
            description = "计算底数的指数幂次方（left ^ right）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.LEFT,
                        label = "底数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.RIGHT,
                        label = "指数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("powerResult"),
                        order = 2,
                    ),
                ),
        )

    val mathSqrt =
        ActionNodeEditorSpec(
            title = "开平方根 (Sqrt)",
            description = "计算数值的非负算术平方根。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "待开方数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sqrtResult"),
                        order = 1,
                    ),
                ),
        )

    val mathSum =
        ActionNodeEditorSpec(
            title = "求和 (Sum)",
            description = "计算数值数组中所有数字的总和。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUES,
                        label = "数字列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sum"),
                        order = 1,
                    ),
                ),
        )

    val mathAvg =
        ActionNodeEditorSpec(
            title = "求平均值 (Average)",
            description = "计算数值数组中所有数字的算术平均值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUES,
                        label = "数字列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("avg"),
                        order = 1,
                    ),
                ),
        )

    val mathLog =
        ActionNodeEditorSpec(
            title = "自然对数 (Log)",
            description = "计算以 e 为底的自然对数值（ln(x)）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "真数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("logResult"),
                        order = 1,
                    ),
                ),
        )

    val mathExp =
        ActionNodeEditorSpec(
            title = "指数运算 (Exp)",
            description = "计算自然常数 e 的 x 次幂（e^x）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "指数",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("expResult"),
                        order = 1,
                    ),
                ),
        )

    val mathNegate =
        ActionNodeEditorSpec(
            title = "求相反数 (Negate)",
            description = "将数值乘以 -1 取相反数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "输入数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("negated"),
                        order = 1,
                    ),
                ),
        )

    val mathRound =
        ActionNodeEditorSpec(
            title = "四舍五入 (Round)",
            description = "将浮点数四舍五入为最接近的整数或指定小数位数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "输入数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("rounded"),
                        order = 1,
                    ),
                ),
        )

    val mathFloor =
        ActionNodeEditorSpec(
            title = "向下取整 (Floor)",
            description = "将浮点数向下舍入为小于或等于该数的最大整数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "输入数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("floored"),
                        order = 1,
                    ),
                ),
        )

    val mathCeil =
        ActionNodeEditorSpec(
            title = "向上取整 (Ceil)",
            description = "将浮点数向上舍入为大于或等于该数的最小整数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "输入数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("ceiled"),
                        order = 1,
                    ),
                ),
        )

    val mathAbs =
        ActionNodeEditorSpec(
            title = "绝对值 (Abs)",
            description = "计算数值的非负绝对值（|x|）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "输入数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("absValue"),
                        order = 1,
                    ),
                ),
        )

    val mathRandom =
        ActionNodeEditorSpec(
            title = "生成随机浮点数 (0~1)",
            description = "生成 [0.0, 1.0) 区间内的随机双精度浮点数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("randomFloat"),
                        order = 0,
                    ),
                ),
        )

    val mathRandomInt =
        ActionNodeEditorSpec(
            title = "生成区间随机整数",
            description = "生成 [min, max] 闭区间内的伪随机整数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.MIN,
                        label = "最小值 (包含)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(1),
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.MAX,
                        label = "最大值 (包含)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(100),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("randomInt"),
                        order = 2,
                    ),
                ),
        )

    val mathClamp =
        ActionNodeEditorSpec(
            title = "数值范围限定 (Clamp)",
            description = "将数值限制在 [min, max] 区间内。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.VALUE,
                        label = "目标数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.MIN,
                        label = "最小值",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.MAX,
                        label = "最大值",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionMathConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("clampedValue"),
                        order = 3,
                    ),
                ),
        )

    // ==================== 文本处理 (Text) ====================

    val textLength =
        ActionNodeEditorSpec(
            title = "获取文本长度",
            description = "计算字符串文本的字符总数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("length"),
                        order = 1,
                    ),
                ),
        )

    val textTrim =
        ActionNodeEditorSpec(
            title = "去除两端空白",
            description = "去除字符串首尾的空格与空白换行符。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("trimmedText"),
                        order = 1,
                    ),
                ),
        )

    val textLowercase =
        ActionNodeEditorSpec(
            title = "转为小写 (Lowercase)",
            description = "将字符串中所有英文字母转换为小写形式。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("lowercaseText"),
                        order = 1,
                    ),
                ),
        )

    val textUppercase =
        ActionNodeEditorSpec(
            title = "转为大写 (Uppercase)",
            description = "将字符串中所有英文字母转换为大写形式。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("uppercaseText"),
                        order = 1,
                    ),
                ),
        )

    val textCapitalize =
        ActionNodeEditorSpec(
            title = "首字母大写 (Capitalize)",
            description = "将字符串的第一个字母转换为大写。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("capitalizedText"),
                        order = 1,
                    ),
                ),
        )

    val textRepeat =
        ActionNodeEditorSpec(
            title = "重复文本",
            description = "将输入字符串重复复制指定次数后输出。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.COUNT,
                        label = "重复次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(2),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("repeatedText"),
                        order = 2,
                    ),
                ),
        )

    val textReverse =
        ActionNodeEditorSpec(
            title = "反转文本",
            description = "将字符串中的字符按倒序排列。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("reversedText"),
                        order = 1,
                    ),
                ),
        )

    val textIndexOf =
        ActionNodeEditorSpec(
            title = "查找子串索引",
            description = "查找子字符串在文本中首次出现的索引下标（从0开始，未找到为-1）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "搜索关键字",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.IGNORE_CASE,
                        label = "忽略大小写",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("index"),
                        order = 3,
                    ),
                ),
        )

    val textTemplate =
        ActionNodeEditorSpec(
            title = "模板字符串替换",
            description = "对文本模板中的占位变量（\${key} 或 {key}）按对象键值进行格式化替换。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEMPLATE,
                        label = "模板内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：你好，\${name}！",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OBJECT,
                        label = "插值变量对象 (JSON)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "{\"name\": \"张三\"}",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("rendered"),
                        order = 2,
                    ),
                ),
        )

    val textSplit =
        ActionNodeEditorSpec(
            title = "拆分文本 (Split)",
            description = "根据指定分隔符将字符串拆分为文本列表数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.DELIMITER,
                        label = "分隔符",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive(","),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("splitItems"),
                        order = 2,
                    ),
                ),
        )

    val textRegexMatch =
        ActionNodeEditorSpec(
            title = "正则表达式匹配",
            description = "使用正则表达式检测文本是否匹配，并提取捕获组内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "正则表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "^[a-zA-Z0-9]+$",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("matchResult"),
                        order = 2,
                    ),
                ),
        )

    val textSubstring =
        ActionNodeEditorSpec(
            title = "截取子字符串",
            description = "按指定的起始下标和结束下标截取字符串的一部分。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.START_INDEX,
                        label = "起始下标",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.END_INDEX,
                        label = "结束下标 (可选)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("subText"),
                        order = 3,
                    ),
                ),
        )

    val textSubstringBefore =
        ActionNodeEditorSpec(
            title = "截取分隔符前内容",
            description = "截取字符串中在指定分隔标记之前出现的子文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.DELIMITER,
                        label = "分隔标记",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("beforeText"),
                        order = 2,
                    ),
                ),
        )

    val textSubstringAfter =
        ActionNodeEditorSpec(
            title = "截取分隔符后内容",
            description = "截取字符串中在指定分隔标记之后出现的子文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.DELIMITER,
                        label = "分隔标记",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("afterText"),
                        order = 2,
                    ),
                ),
        )

    val textReplace =
        ActionNodeEditorSpec(
            title = "文本替换 (Replace)",
            description = "将文本中匹配的子串替换为新字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "查找内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.REPLACEMENT,
                        label = "替换为",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("replacedText"),
                        order = 3,
                    ),
                ),
        )

    val textReplaceRegex =
        ActionNodeEditorSpec(
            title = "正则替换 (Regex Replace)",
            description = "使用正则表达式匹配文本并替换为新内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "正则表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.REPLACEMENT,
                        label = "替换为",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("regexReplacedText"),
                        order = 3,
                    ),
                ),
        )

    val textJoin =
        ActionNodeEditorSpec(
            title = "文本数组拼接 (Join)",
            description = "使用指定连接符将文本列表拼接为单个字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.VALUES,
                        label = "文本列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.SEPARATOR,
                        label = "连接符",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive(","),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("joinedText"),
                        order = 2,
                    ),
                ),
        )

    val textMatchAll =
        ActionNodeEditorSpec(
            title = "正则全局匹配 (Match All)",
            description = "使用正则表达式在文本中全局查找所有匹配项列表。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "正则表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("allMatches"),
                        order = 2,
                    ),
                ),
        )

    val textPad =
        ActionNodeEditorSpec(
            title = "文本对齐填充 (Pad)",
            description = "使用指定字符对字符串进行前置或后置长度填充。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PAD_LENGTH,
                        label = "目标总长度",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PAD_CHARACTER,
                        label = "填充字符",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive(" "),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PAD_END,
                        label = "在末尾填充 (右对齐)",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("paddedText"),
                        order = 4,
                    ),
                ),
        )

    val textFormatNumber =
        ActionNodeEditorSpec(
            title = "数字格式化为文本",
            description = "将数值按指定小数位数格式化为文本字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入数值/文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.FRACTION_DIGITS,
                        label = "保留小数位数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(2),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("formattedNumber"),
                        order = 2,
                    ),
                ),
        )

    val textContains =
        ActionNodeEditorSpec(
            title = "判断包含子串 (Contains)",
            description = "检查文本中是否包含指定的子字符串（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "匹配子串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.IGNORE_CASE,
                        label = "忽略大小写",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("contains"),
                        order = 3,
                    ),
                ),
        )

    val textStartsWith =
        ActionNodeEditorSpec(
            title = "判断前缀 (StartsWith)",
            description = "检查文本是否以指定子串开头（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "前缀子串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.IGNORE_CASE,
                        label = "忽略大小写",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("startsWith"),
                        order = 3,
                    ),
                ),
        )

    val textEndsWith =
        ActionNodeEditorSpec(
            title = "判断后缀 (EndsWith)",
            description = "检查文本是否以指定子串结尾（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.PATTERN,
                        label = "后缀子串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.IGNORE_CASE,
                        label = "忽略大小写",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("endsWith"),
                        order = 3,
                    ),
                ),
        )

    val textSlugify =
        ActionNodeEditorSpec(
            title = "转换为 URL Slug",
            description = "将文本转换为适用于 URL 的小写连字符 Slug 格式。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("slug"),
                        order = 1,
                    ),
                ),
        )

    val textTruncate =
        ActionNodeEditorSpec(
            title = "截断文本 (Truncate)",
            description = "将超长文本截断至指定长度并在末尾追加省略号...",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.TEXT,
                        label = "输入文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.LIMIT,
                        label = "最大截断长度",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(50),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.ELLIPSIS,
                        label = "省略符号",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive("..."),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionTextConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("truncatedText"),
                        order = 3,
                    ),
                ),
        )

    // ==================== 对象处理 (Object) ====================

    val objectGet =
        ActionNodeEditorSpec(
            title = "获取对象属性 (Get)",
            description = "从 JSON 对象中获取指定路径或键名的属性值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.PATH,
                        label = "属性路径 / 键名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：user.address.city",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("propValue"),
                        order = 2,
                    ),
                ),
        )

    val objectSet =
        ActionNodeEditorSpec(
            title = "设置对象属性 (Set)",
            description = "在 JSON 对象中按指定键名设置属性值并返回新对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.KEY,
                        label = "属性键名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.VALUE,
                        label = "设置的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("updatedObject"),
                        order = 3,
                    ),
                ),
        )

    val objectRemove =
        ActionNodeEditorSpec(
            title = "删除对象属性 (Remove)",
            description = "从 JSON 对象中删除指定属性并返回新对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.KEY,
                        label = "删除的键名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("prunedObject"),
                        order = 2,
                    ),
                ),
        )

    val objectOmit =
        ActionNodeEditorSpec(
            title = "排除指定属性 (Omit)",
            description = "从 JSON 对象中排除一组指定键名，保留其余属性。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.KEYS,
                        label = "排除的键列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("omittedObject"),
                        order = 2,
                    ),
                ),
        )

    val objectPick =
        ActionNodeEditorSpec(
            title = "提取指定属性 (Pick)",
            description = "从 JSON 对象中仅提取一组指定键名的属性。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.KEYS,
                        label = "提取的键列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("pickedObject"),
                        order = 2,
                    ),
                ),
        )

    val objectMerge =
        ActionNodeEditorSpec(
            title = "合并多个对象 (Merge)",
            description = "将对象列表中的多个 JSON 对象合并为一个新对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECTS,
                        label = "待合并对象列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("mergedObject"),
                        order = 1,
                    ),
                ),
        )

    val objectKeys =
        ActionNodeEditorSpec(
            title = "获取对象键列表 (Keys)",
            description = "获取 JSON 对象所有顶层键名构成的字符串数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("keys"),
                        order = 1,
                    ),
                ),
        )

    val objectValues =
        ActionNodeEditorSpec(
            title = "获取对象值列表 (Values)",
            description = "获取 JSON 对象所有顶层属性值构成的数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("values"),
                        order = 1,
                    ),
                ),
        )

    val objectEntries =
        ActionNodeEditorSpec(
            title = "获取键值对列表 (Entries)",
            description = "将 JSON 对象转换为 [key, value] 键值对构成的二维数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("entries"),
                        order = 1,
                    ),
                ),
        )

    val objectFromEntries =
        ActionNodeEditorSpec(
            title = "键值对构造对象 (FromEntries)",
            description = "将 [key, value] 键值对二维数组转换为 JSON 对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.ENTRIES,
                        label = "键值对二维数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("constructedObject"),
                        order = 1,
                    ),
                ),
        )

    val objectHasKey =
        ActionNodeEditorSpec(
            title = "检查包含属性 (HasKey)",
            description = "判断 JSON 对象是否包含指定键名（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.KEY,
                        label = "属性键名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hasKey"),
                        order = 2,
                    ),
                ),
        )

    val objectIsEmpty =
        ActionNodeEditorSpec(
            title = "判断对象为空 (IsEmpty)",
            description = "检查 JSON 对象是否不包含任何键值对（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OBJECT,
                        label = "目标对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionObjectConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("isEmpty"),
                        order = 1,
                    ),
                ),
        )

    // ==================== 数组处理 (Array) ====================

    val arrayLength =
        ActionNodeEditorSpec(
            title = "获取数组长度",
            description = "计算 JSON 数组中的元素总个数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("length"),
                        order = 1,
                    ),
                ),
        )

    val arrayCreate =
        ActionNodeEditorSpec(
            title = "创建/转换数组",
            description = "将输入数据结构构造成 JSON 数组格式。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "初始数组元素 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("array"),
                        order = 1,
                    ),
                ),
        )

    val arrayAppend =
        ActionNodeEditorSpec(
            title = "追加数组元素 (Append)",
            description = "向 JSON 数组末尾（或指定索引位置）插入新元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUE,
                        label = "追加的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.INDEX,
                        label = "插入位置下标 (可选，默认末尾)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("appendedArray"),
                        order = 3,
                    ),
                ),
        )

    val arrayInsertAt =
        ActionNodeEditorSpec(
            title = "指定位置插入元素 (InsertAt)",
            description = "在 JSON 数组指定下标索引处插入新元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.INDEX,
                        label = "插入位置下标",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUE,
                        label = "插入的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("updatedArray"),
                        order = 3,
                    ),
                ),
        )

    val arrayRemoveAt =
        ActionNodeEditorSpec(
            title = "移除指定位置元素 (RemoveAt)",
            description = "从 JSON 数组中移除指定索引位置的元素并返回新数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.INDEX,
                        label = "移除位置下标",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("reducedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayFilter =
        ActionNodeEditorSpec(
            title = "数组条件筛选 (Filter)",
            description = "根据属性路径与判断运算符筛选满足条件的数组元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "元素属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "例如：$.status 或 age",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OPERATOR,
                        label = "筛选运算符",
                        kind = ActionEditorFieldKind.SELECT,
                        required = true,
                        defaultValue = JsonPrimitive(ActionArrayFilterOperator.EQUALS),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.EQUALS), "等于 (==)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.NOT_EQUALS), "不等于 (!=)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.IS_NULL), "为空 (null)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.IS_NOT_NULL), "不为空 (not null)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.IS_EMPTY), "为空白"),
                                ActionEditorFieldOption(JsonPrimitive(ActionArrayFilterOperator.IS_NOT_EMPTY), "不为空白"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.EXPECTED,
                        label = "目标预期值",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("filteredArray"),
                        order = 4,
                    ),
                ),
        )

    val arrayMap =
        ActionNodeEditorSpec(
            title = "数组属性提取 (Map)",
            description = "按指定的属性路径提取数组中每个对象的字段值，组成新数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "提取属性路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.id 或 name",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("mappedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayFlatMap =
        ActionNodeEditorSpec(
            title = "数组平铺提取 (FlatMap)",
            description = "提取数组中每个元素的嵌套数组属性并平铺扁平化输出。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "平铺提取路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.tags",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("flatMappedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayConcat =
        ActionNodeEditorSpec(
            title = "数组拼接合并 (Concat)",
            description = "将包含多个子数组的数组平铺合并为一个扁平的长数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "待合并的数组列表",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("concatenatedArray"),
                        order = 1,
                    ),
                ),
        )

    val arrayZip =
        ActionNodeEditorSpec(
            title = "数组压缩配对 (Zip)",
            description = "将两个数组按相同索引配对打包为 [left[i], right[i]] 二元组数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "左侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OTHER_VALUES,
                        label = "右侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("zippedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayTake =
        ActionNodeEditorSpec(
            title = "截取前 N 个元素 (Take)",
            description = "从数组开头截取前 count 个元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.COUNT,
                        label = "截取数量",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(5),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("takenArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayDrop =
        ActionNodeEditorSpec(
            title = "丢弃前 N 个元素 (Drop)",
            description = "跳过数组开头的 count 个元素，返回剩余元素构成的数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.COUNT,
                        label = "丢弃数量",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(1),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("droppedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayContains =
        ActionNodeEditorSpec(
            title = "判断数组包含 (Contains)",
            description = "检查数组中是否存在指定的值（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUE,
                        label = "待检查的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("contains"),
                        order = 2,
                    ),
                ),
        )

    val arrayFind =
        ActionNodeEditorSpec(
            title = "查找首个匹配元素 (Find)",
            description = "查找数组中首个属性等于指定值的元素并输出。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "属性路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.id",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.EXPECTED,
                        label = "目标匹配值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("foundItem"),
                        order = 3,
                    ),
                ),
        )

    val arrayDistinct =
        ActionNodeEditorSpec(
            title = "数组元素去重 (Distinct)",
            description = "去除数组中的所有重复项，保留唯一元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("distinctArray"),
                        order = 1,
                    ),
                ),
        )

    val arraySort =
        ActionNodeEditorSpec(
            title = "数组排序 (Sort)",
            description = "对数组元素进行升序或降序排序（支持指定对象属性排序）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "排序属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "例如：$.price",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.DESCENDING,
                        label = "是否降序排列",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sortedArray"),
                        order = 3,
                    ),
                ),
        )

    val arrayReverse =
        ActionNodeEditorSpec(
            title = "数组反转 (Reverse)",
            description = "将数组中的所有元素顺序倒转。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("reversedArray"),
                        order = 1,
                    ),
                ),
        )

    val arraySlice =
        ActionNodeEditorSpec(
            title = "数组切片 (Slice)",
            description = "按指定的起始下标和结束下标截取数组的一个切片。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.START,
                        label = "起始下标 (可选，默认0)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.END,
                        label = "结束下标 (可选，不包含)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("slicedArray"),
                        order = 3,
                    ),
                ),
        )

    val arrayFlatten =
        ActionNodeEditorSpec(
            title = "多维数组平铺 (Flatten)",
            description = "将嵌套的二维或多维数组平铺为一维数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标嵌套数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("flattenedArray"),
                        order = 1,
                    ),
                ),
        )

    val arrayGroupBy =
        ActionNodeEditorSpec(
            title = "数组分组 (GroupBy)",
            description = "根据指定的属性键名将对象数组分组为 Map 映射。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标对象数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "分组依据属性路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.category",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("groupedMap"),
                        order = 2,
                    ),
                ),
        )

    val arrayReduce =
        ActionNodeEditorSpec(
            title = "数组归约累加 (Reduce)",
            description = "对数组元素执行累积归约计算，输出最终单一汇总结果。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.INITIAL_VALUE,
                        label = "初始值 (可选)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("reducedResult"),
                        order = 2,
                    ),
                ),
        )

    val arrayFirst =
        ActionNodeEditorSpec(
            title = "获取首个元素 (First)",
            description = "提取数组中的第一个元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("firstItem"),
                        order = 1,
                    ),
                ),
        )

    val arrayLast =
        ActionNodeEditorSpec(
            title = "获取末尾元素 (Last)",
            description = "提取数组中的最后一个元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("lastItem"),
                        order = 1,
                    ),
                ),
        )

    val arraySum =
        ActionNodeEditorSpec(
            title = "数组元素求和 (Sum)",
            description = "计算数值数组或对象数组中指定属性字段的总和。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "数字数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "数字属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "对象数组时填属性名，如 price",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sum"),
                        order = 2,
                    ),
                ),
        )

    val arrayAvg =
        ActionNodeEditorSpec(
            title = "数组求平均值 (Average)",
            description = "计算数值数组或对象数组中指定属性字段的算术平均值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "数字数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "数字属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "对象数组时填属性名，如 price",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("avg"),
                        order = 2,
                    ),
                ),
        )

    val arrayMin =
        ActionNodeEditorSpec(
            title = "数组最小值 (Min)",
            description = "计算数值数组或对象数组中指定属性字段的最小值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "数字数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "数字属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "对象数组时填属性名，如 price",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("minVal"),
                        order = 2,
                    ),
                ),
        )

    val arrayMax =
        ActionNodeEditorSpec(
            title = "数组最大值 (Max)",
            description = "计算数值数组或对象数组中指定属性字段的最大值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "数字数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.FIELD_PATH,
                        label = "数字属性路径 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "对象数组时填属性名，如 price",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("maxVal"),
                        order = 2,
                    ),
                ),
        )

    val arrayChunk =
        ActionNodeEditorSpec(
            title = "数组分块拆分 (Chunk)",
            description = "将数组拆分成指定大小的一组较小数组块。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.SIZE,
                        label = "分块大小",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(10),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("chunkedArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayShuffle =
        ActionNodeEditorSpec(
            title = "数组随机乱序 (Shuffle)",
            description = "将数组中所有元素的顺序随机打乱。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("shuffledArray"),
                        order = 1,
                    ),
                ),
        )

    val arraySample =
        ActionNodeEditorSpec(
            title = "随机采样元素 (Sample)",
            description = "从数组中随机抽取一个或多个样本元素。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.COUNT,
                        label = "采样个数 (默认1)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(1),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("sampledItems"),
                        order = 2,
                    ),
                ),
        )

    val arrayIndexOf =
        ActionNodeEditorSpec(
            title = "获取元素索引 (IndexOf)",
            description = "在数组中查找指定元素首次出现的索引下标（未找到返回 -1）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "目标数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUE,
                        label = "待查找的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("index"),
                        order = 2,
                    ),
                ),
        )

    val arrayIntersection =
        ActionNodeEditorSpec(
            title = "数组交集 (Intersection)",
            description = "计算两个数组中共有的元素集合。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "左侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OTHER_VALUES,
                        label = "右侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("intersectionArray"),
                        order = 2,
                    ),
                ),
        )

    val arrayDifference =
        ActionNodeEditorSpec(
            title = "数组差集 (Difference)",
            description = "计算存在于左侧数组但不存在于右侧数组中的元素集合。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.VALUES,
                        label = "左侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OTHER_VALUES,
                        label = "右侧数组",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionArrayConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("diffArray"),
                        order = 2,
                    ),
                ),
        )
}
