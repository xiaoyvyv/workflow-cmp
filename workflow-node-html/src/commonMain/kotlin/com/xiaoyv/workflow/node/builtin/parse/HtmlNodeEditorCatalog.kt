package com.xiaoyv.workflow.node.builtin.parse

import com.xiaoyv.workflow.model.spec.ActionHtmlConfigKey
import com.xiaoyv.workflow.model.spec.ActionHtmlOperation
import com.xiaoyv.workflow.model.spec.ActionXmlConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

/**
 * HTML 与 XML 模块的集中编辑器说明目录。
 */
internal object HtmlNodeEditorCatalog {
    // ==================== HTML 解析与提取 ====================

    val htmlParse =
        ActionNodeEditorSpec(
            title = "解析与格式化 HTML",
            description = "解析 HTML 源码字符串并格式化为标准规范的 HTML 文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "HTML 源码",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "输入 HTML 源码或引用变量 {{vars.html}}",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("parsedHtml"),
                        order = 1,
                    ),
                ),
        )

    val htmlSelect =
        ActionNodeEditorSpec(
            title = "CSS 选择器多项提取",
            description = "使用 CSS 选择器从 HTML 中提取所有匹配的元素，输出 outerHtml 字符串数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "默认使用上游节点输入的 HTML 内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.SELECTOR,
                        label = "CSS 选择器",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：div.content a.link",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("elements"),
                        order = 2,
                    ),
                ),
        )

    val htmlSelectFirst =
        ActionNodeEditorSpec(
            title = "CSS 选择器首项提取",
            description = "使用 CSS 选择器从 HTML 中提取首个匹配的元素，输出其 outerHtml 字符串。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "默认使用上游节点输入的 HTML 内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.SELECTOR,
                        label = "CSS 选择器",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：h1.title",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("element"),
                        order = 2,
                    ),
                ),
        )

    val htmlAttr =
        ActionNodeEditorSpec(
            title = "提取 HTML 属性值",
            description = "提取 HTML 标签的指定属性值（如 href、src、title 等）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "默认使用上游节点输入的 HTML 内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.ATTRIBUTE,
                        label = "属性名称",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：href、src、data-id",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("attrValue"),
                        order = 2,
                    ),
                ),
        )

    val htmlTag =
        ActionNodeEditorSpec(
            title = "提取 HTML 标签名",
            description = "获取 HTML 元素的标签名称（如 div、a、span 等）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("tagName"),
                        order = 1,
                    ),
                ),
        )

    val htmlText =
        ActionNodeEditorSpec(
            title = "提取纯文本内容",
            description = "从 HTML 中剥离所有标签并提取纯文本内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("text"),
                        order = 1,
                    ),
                ),
        )

    val htmlData =
        ActionNodeEditorSpec(
            title = "提取 Script/Style 数据",
            description = "提取 script、style 等标签内部包含的脚本代码或样式数据内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("data"),
                        order = 1,
                    ),
                ),
        )

    val htmlValue =
        ActionNodeEditorSpec(
            title = "提取表单控件值",
            description = "提取 input、textarea 等表单控件元素的 value 属性值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("value"),
                        order = 1,
                    ),
                ),
        )

    val htmlId =
        ActionNodeEditorSpec(
            title = "提取元素 ID",
            description = "获取 HTML 元素的 id 属性值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("id"),
                        order = 1,
                    ),
                ),
        )

    val htmlHtml =
        ActionNodeEditorSpec(
            title = "提取内部 HTML (innerHTML)",
            description = "提取 HTML 元素的内部 HTML 内容（不含外层标签自身）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("innerHtml"),
                        order = 1,
                    ),
                ),
        )

    val htmlOuterHtml =
        ActionNodeEditorSpec(
            title = "提取外部 HTML (outerHTML)",
            description = "提取 HTML 元素的完整外部 HTML 内容（包含外层标签）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("outerHtml"),
                        order = 1,
                    ),
                ),
        )

    val htmlHasClass =
        ActionNodeEditorSpec(
            title = "判断包含 CSS 类名",
            description = "检查 HTML 元素是否包含指定的 class 类名（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.CLASS_NAME,
                        label = "CSS 类名 (Class Name)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：active、highlight",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hasClass"),
                        order = 2,
                    ),
                ),
        )

    val htmlMap =
        ActionNodeEditorSpec(
            title = "批量提取集合属性 (Map)",
            description = "遍历 HTML 元素集合，批量提取每个元素的文本、属性或子内容构成数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 集合 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OPERATION,
                        label = "提取操作类型",
                        kind = ActionEditorFieldKind.SELECT,
                        required = true,
                        defaultValue = JsonPrimitive(ActionHtmlOperation.TEXT),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.TEXT), "提取纯文本 (text)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.ATTR), "提取指定属性 (attr)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.TAG), "提取标签名 (tag)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.HTML), "提取内部 HTML (html)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.OUTER_HTML), "提取完整 HTML (outerHtml)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.ID), "提取 ID 属性 (id)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.DATA), "提取数据内容 (data)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHtmlOperation.VALUE), "提取表单值 (value)"),
                            ),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.ATTRIBUTE,
                        label = "属性名称 (当操作为 attr 时必填)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "例如：href、src",
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("mappedList"),
                        order = 3,
                    ),
                ),
        )

    val htmlFirst =
        ActionNodeEditorSpec(
            title = "提取首个元素 (First)",
            description = "从 HTML 元素集合中取出第一个元素的 outerHtml。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 集合 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("firstElement"),
                        order = 1,
                    ),
                ),
        )

    val htmlLast =
        ActionNodeEditorSpec(
            title = "提取末尾元素 (Last)",
            description = "从 HTML 元素集合中取出最后一个元素的 outerHtml。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 集合 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("lastElement"),
                        order = 1,
                    ),
                ),
        )

    val htmlGet =
        ActionNodeEditorSpec(
            title = "按索引提取元素 (Get)",
            description = "按从 0 开始的下标索引从 HTML 集合中取出对应元素的 outerHtml。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 集合 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.INDEX,
                        label = "元素下标索引",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = true,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("element"),
                        order = 2,
                    ),
                ),
        )

    val htmlSize =
        ActionNodeEditorSpec(
            title = "获取元素集合数量 (Size)",
            description = "统计 HTML 元素集合中的元素总个数。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 集合 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("size"),
                        order = 1,
                    ),
                ),
        )

    val htmlRemove =
        ActionNodeEditorSpec(
            title = "剔除指定标签 (Remove)",
            description = "根据 CSS 选择器匹配并移除不需要的标签节点，返回清洗后的 HTML 源码。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.SELECTOR,
                        label = "待移除的 CSS 选择器",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：script, style, .ad-banner",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("cleanedHtml"),
                        order = 2,
                    ),
                ),
        )

    val htmlParent =
        ActionNodeEditorSpec(
            title = "提取父级元素 (Parent)",
            description = "获取 HTML 元素的直接父级标签 outerHtml。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("parent"),
                        order = 1,
                    ),
                ),
        )

    val htmlChildren =
        ActionNodeEditorSpec(
            title = "提取子元素列表 (Children)",
            description = "获取 HTML 元素的所有直接子元素 outerHtml 数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("children"),
                        order = 1,
                    ),
                ),
        )

    val htmlTableToJson =
        ActionNodeEditorSpec(
            title = "表格解析为 JSON (Table to JSON)",
            description = "将 HTML 中的 <table> 表格数据自动按表头解析为 JSON 对象数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.HTML,
                        label = "输入 HTML 源码",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.SELECTOR,
                        label = "表格 CSS 选择器",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive("table"),
                        placeholder = "默认：table",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHtmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("tableData"),
                        order = 2,
                    ),
                ),
        )

    // ==================== XML 处理 ====================

    val xmlParse =
        ActionNodeEditorSpec(
            title = "解析 XML 为 JSON",
            description = "将 XML / RSS 文本格式化解析为层级 JSON 结构化对象。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionXmlConfigKey.TEXT,
                        label = "XML 文本内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "<root><item>value</item></root>",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionXmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("parsedXml"),
                        order = 1,
                    ),
                ),
        )

    val xmlStringify =
        ActionNodeEditorSpec(
            title = "JSON 转换为 XML",
            description = "将结构化 JSON 对象序列化转换生成标准 XML 文本。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionXmlConfigKey.DATA,
                        label = "待转换的 JSON 数据对象",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "{\"root\": {\"item\": \"value\"}}",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionXmlConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("xmlString"),
                        order = 1,
                    ),
                ),
        )
}
