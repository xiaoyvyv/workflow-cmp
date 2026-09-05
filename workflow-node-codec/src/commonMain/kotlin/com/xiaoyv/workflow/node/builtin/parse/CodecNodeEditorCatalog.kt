package com.xiaoyv.workflow.node.builtin.parse

import com.xiaoyv.workflow.model.spec.ActionCodecConfigKey
import com.xiaoyv.workflow.model.spec.ActionCsvConfigKey
import com.xiaoyv.workflow.model.spec.ActionDateConfigKey
import com.xiaoyv.workflow.model.spec.ActionDateTimeUnit
import com.xiaoyv.workflow.model.spec.ActionJsonConfigKey
import com.xiaoyv.workflow.model.spec.ActionUrlConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

/**
 * 编解码、JSON、CSV、URL 及日期时间模块的集中编辑器说明目录。
 */
internal object CodecNodeEditorCatalog {
    // ==================== 编解码 (Codec) ====================

    val base64Encode =
        ActionNodeEditorSpec(
            title = "Base64 编码",
            description = "将纯文本或字符串编码为标准 Base64 格式字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "待编码文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "输入需要进行 Base64 编码的文本",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("base64Encoded"),
                        order = 1,
                    ),
                ),
        )

    val base64Decode =
        ActionNodeEditorSpec(
            title = "Base64 解码",
            description = "将标准 Base64 格式字符串解码还原为原始文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "Base64 密文",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "输入需要解码的 Base64 字符串",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("base64Decoded"),
                        order = 1,
                    ),
                ),
        )

    val base64UrlEncode =
        ActionNodeEditorSpec(
            title = "Base64URL 编码",
            description = "将文本编码为 URL 安全的 Base64URL 格式（无填充符）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "待编码文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("base64UrlEncoded"),
                        order = 1,
                    ),
                ),
        )

    val base64UrlDecode =
        ActionNodeEditorSpec(
            title = "Base64URL 解码",
            description = "将 URL 安全的 Base64URL 字符串解码还原为文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "Base64URL 字符串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("base64UrlDecoded"),
                        order = 1,
                    ),
                ),
        )

    val hexEncode =
        ActionNodeEditorSpec(
            title = "Hex 十六进制编码",
            description = "将文本字符串转换为十六进制 Hex 编码字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "待编码文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hexEncoded"),
                        order = 1,
                    ),
                ),
        )

    val hexDecode =
        ActionNodeEditorSpec(
            title = "Hex 十六进制解码",
            description = "将十六进制 Hex 编码字符串还原为原始文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "Hex 字符串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hexDecoded"),
                        order = 1,
                    ),
                ),
        )

    val urlEncode =
        ActionNodeEditorSpec(
            title = "URL 编码",
            description = "对字符串执行 URL 编码（百分号编码），以便在 URL 参数中安全传输。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "待编码文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("urlEncoded"),
                        order = 1,
                    ),
                ),
        )

    val urlDecode =
        ActionNodeEditorSpec(
            title = "URL 解码",
            description = "对 URL 编码（百分号编码）的字符串进行解码还原。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "URL 编码字符串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("urlDecoded"),
                        order = 1,
                    ),
                ),
        )

    val htmlEscape =
        ActionNodeEditorSpec(
            title = "HTML 转义",
            description = "转义 HTML 特殊字符（&, <, >, \", '），防止注入。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "待转义文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("htmlEscaped"),
                        order = 1,
                    ),
                ),
        )

    val htmlUnescape =
        ActionNodeEditorSpec(
            title = "HTML 反转义",
            description = "还原 HTML 实体（如 &amp;, &lt;, &gt;）为原始字符。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.TEXT,
                        label = "HTML 实体文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCodecConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("htmlUnescaped"),
                        order = 1,
                    ),
                ),
        )

    // ==================== JSON 处理 ====================

    val jsonExtract =
        ActionNodeEditorSpec(
            title = "JSONPath 提取",
            description = "使用 JSONPath 表达式从 JSON 对象或数组中提取匹配的数据。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.SOURCE,
                        label = "源数据 (JSON / 字符串)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "输入待提取的 JSON 对象、数组或变量",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.PATH,
                        label = "JSONPath 表达式",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：$.data.items[0].id",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("extracted"),
                        order = 2,
                    ),
                ),
        )

    val jsonParse =
        ActionNodeEditorSpec(
            title = "解析 JSON 文本",
            description = "将 JSON 格式字符串反序列化为结构化的 JSON 对象或数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.TEXT,
                        label = "JSON 文本",
                        kind = ActionEditorFieldKind.TEXTAREA,
                        required = true,
                        placeholder = "输入合法的 JSON 字符串",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("jsonParsed"),
                        order = 1,
                    ),
                ),
        )

    val jsonStringify =
        ActionNodeEditorSpec(
            title = "序列化为 JSON 文本",
            description = "将结构化的变量、对象或数组序列化转换为 JSON 格式字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.VALUE,
                        label = "待序列化的值",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "选择或输入要转换的对象/变量",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("jsonString"),
                        order = 1,
                    ),
                ),
        )

    val jsonValidate =
        ActionNodeEditorSpec(
            title = "校验 JSON 语法",
            description = "检查输入文本是否为合法的 JSON 格式字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.TEXT,
                        label = "JSON 文本",
                        kind = ActionEditorFieldKind.TEXTAREA,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("isValidJson"),
                        order = 1,
                    ),
                ),
        )

    val jsonSchemaValidate =
        ActionNodeEditorSpec(
            title = "JSON Schema 校验",
            description = "使用 JSON Schema 校验 JSON 数据是否符合指定的结构契约。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.VALUE,
                        label = "待校验的 JSON 数据",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.SCHEMA,
                        label = "JSON Schema 定义",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionJsonConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("schemaValid"),
                        order = 2,
                    ),
                ),
        )

    // ==================== CSV 处理 ====================

    val csvParse =
        ActionNodeEditorSpec(
            title = "解析 CSV 文本",
            description = "将 CSV 格式表格文本解析为对象列表或数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.TEXT,
                        label = "CSV 文本内容",
                        kind = ActionEditorFieldKind.TEXTAREA,
                        required = true,
                        placeholder = "输入 CSV 格式数据",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.DELIMITER,
                        label = "分隔符",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive(","),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.HEADER_ROW,
                        label = "首行作为表头",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(true),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("csvRows"),
                        order = 3,
                    ),
                ),
        )

    val csvStringify =
        ActionNodeEditorSpec(
            title = "导出为 CSV 文本",
            description = "将对象列表或二维数组导出为 CSV 格式字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.ITEMS,
                        label = "行数据列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "例如：[{\"name\":\"Alice\",\"age\":20}]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.DELIMITER,
                        label = "分隔符",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive(","),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionCsvConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("csvText"),
                        order = 2,
                    ),
                ),
        )

    // ==================== URL 处理 ====================

    val urlParse =
        ActionNodeEditorSpec(
            title = "解析 URL",
            description = "解析 URL 字符串为协议、主机、路径、查询参数等结构化对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.URL,
                        label = "URL 地址",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://example.com/api?id=1",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("parsedUrl"),
                        order = 1,
                    ),
                ),
        )

    val urlBuild =
        ActionNodeEditorSpec(
            title = "构造 URL",
            description = "根据 Base URL 与 Query 查询参数动态拼接构造完整 URL。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.BASE_URL,
                        label = "基础 URL (Base URL)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://api.example.com/v1/search",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.QUERY_PARAMETERS,
                        label = "Query 参数 (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "{\"keyword\": \"kotlin\", \"page\": 1}",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("builtUrl"),
                        order = 2,
                    ),
                ),
        )

    val urlSetQueryParam =
        ActionNodeEditorSpec(
            title = "设置 URL 查询参数",
            description = "在已有 URL 中添加或替换指定的 Query 参数并输出新 URL。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.URL,
                        label = "原始 URL",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.KEY,
                        label = "参数名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.VALUE,
                        label = "参数值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("updatedUrl"),
                        order = 3,
                    ),
                ),
        )

    val urlGetQueryParam =
        ActionNodeEditorSpec(
            title = "获取 URL 查询参数",
            description = "从给定 URL 中提取指定 Query 参数的值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.URL,
                        label = "URL 地址",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.KEY,
                        label = "参数名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionUrlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("queryParamValue"),
                        order = 2,
                    ),
                ),
        )

    // ==================== 日期时间 (Date) ====================

    val dateNow =
        ActionNodeEditorSpec(
            title = "获取当前时间",
            description = "获取系统当前时间戳（毫秒）并输出。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("timestamp"),
                        placeholder = "存放当前毫秒时间戳的字段名",
                        order = 0,
                    ),
                ),
        )

    val dateFormat =
        ActionNodeEditorSpec(
            title = "格式化日期",
            description = "将毫秒时间戳转换为 ISO-8601 日期时间字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP,
                        label = "毫秒时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "输入毫秒时间戳",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("formattedDate"),
                        order = 1,
                    ),
                ),
        )

    val dateParse =
        ActionNodeEditorSpec(
            title = "解析日期为时间戳",
            description = "将 ISO-8601 日期时间字符串解析为毫秒时间戳。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TEXT,
                        label = "ISO 日期字符串",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：2026-09-05T09:00:00Z",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("timestamp"),
                        order = 1,
                    ),
                ),
        )

    val dateAdd =
        ActionNodeEditorSpec(
            title = "增加时间量",
            description = "为指定时间戳增加指定的时间量（天、小时、分钟、秒、毫秒）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP,
                        label = "基准时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.COUNT,
                        label = "增加数量",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(1),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.UNIT,
                        label = "时间单位",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionDateTimeUnit.DAYS),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.DAYS), "天"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.HOURS), "小时"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MINUTES), "分钟"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.SECONDS), "秒"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MILLISECONDS), "毫秒"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("addedTimestamp"),
                        order = 3,
                    ),
                ),
        )

    val dateSubtract =
        ActionNodeEditorSpec(
            title = "减少时间量",
            description = "为指定时间戳减去指定的时间量（天、小时、分钟、秒、毫秒）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP,
                        label = "基准时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.COUNT,
                        label = "减少数量",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(1),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.UNIT,
                        label = "时间单位",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionDateTimeUnit.DAYS),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.DAYS), "天"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.HOURS), "小时"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MINUTES), "分钟"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.SECONDS), "秒"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MILLISECONDS), "毫秒"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("subtractedTimestamp"),
                        order = 3,
                    ),
                ),
        )

    val dateDiff =
        ActionNodeEditorSpec(
            title = "计算时间差值",
            description = "计算两个毫秒时间戳之间的差值（按指定单位输出数值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP_LEFT,
                        label = "左侧时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP_RIGHT,
                        label = "右侧时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.UNIT,
                        label = "计算差值单位",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionDateTimeUnit.DAYS),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.DAYS), "天"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.HOURS), "小时"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MINUTES), "分钟"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.SECONDS), "秒"),
                                ActionEditorFieldOption(JsonPrimitive(ActionDateTimeUnit.MILLISECONDS), "毫秒"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("dateDiff"),
                        order = 3,
                    ),
                ),
        )

    val dateRelativeTime =
        ActionNodeEditorSpec(
            title = "相对时间描述",
            description = "计算毫秒时间戳相对于当前时间的友好描述（如“刚刚”、“3分钟前”、“2天后”）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP,
                        label = "目标时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("relativeTime"),
                        order = 1,
                    ),
                ),
        )

    val dateGetComponent =
        ActionNodeEditorSpec(
            title = "提取日期分量",
            description = "从时间戳中提取年、月、日、时、分、秒等分量对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.TIMESTAMP,
                        label = "目标时间戳",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionDateConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("dateComponent"),
                        order = 1,
                    ),
                ),
        )
}
