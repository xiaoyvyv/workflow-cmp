package com.xiaoyv.workflow.node.core

import com.xiaoyv.workflow.Immutable
import kotlinx.serialization.Serializable

/**
 * 节点分类描述规格。
 */
@Immutable
@Serializable
data class ActionCategorySpec(
    val id: String,
    val label: String,
    val description: String = "",
)

/**
 * 内置节点面向编辑器展示与筛选的稳定分类。
 *
 * 分类值会被导出的工作流编辑器配置、节点面板和搜索索引使用；节点定义不得直接书写分类字面量。
 */
object ActionNodeCategory {
    /**
     * 流程的入口、终止、延迟与断言。
     */
    const val FLOW = "flow"

    /**
     * 条件、比较及分支判断。
     */
    const val CONTROL = "control"

    /**
     * 循环及循环体控制。
     */
    const val LOOP = "loop"

    /**
     * 基础数据读写与数据合并。
     */
    const val DATA = "data"

    /**
     * 日期与时间转换。
     */
    const val DATE = "date"

    /**
     * HTML 文档和 CSS 选择器解析。
     */
    const val HTML = "html"

    /**
     * HTTP 请求及响应处理。
     */
    const val HTTP = "http"

    /**
     * 工作流私有持久化存储。
     */
    const val STORAGE = "storage"

    /**
     * JSON 对象字段操作。
     */
    const val OBJECT = "object"

    /**
     * JSON 文本与结构化数据转换。
     */
    const val JSON = "json"

    /**
     * 二进制与文本编解码。
     */
    const val CODEC = "codec"

    /**
     * 不可逆摘要计算。
     */
    const val CRYPTO = "crypto"

    /**
     * 字符串、正则与 URL 文本处理。
     */
    const val TEXT = "text"

    /**
     * JSON 数组处理。
     */
    const val ARRAY = "array"

    /**
     * 数值运算。
     */
    const val MATH = "math"

    /**
     * 需要宿主授权或执行的网络、导航与提示动作。
     */
    const val ACTION = "action"

    /**
     * URL 解析与构建。
     */
    const val URL = "url"

    /**
     * CSV 格式转换。
     */
    const val CSV = "csv"

    /**
     * XML / RSS 转换。
     */
    const val XML = "xml"

    /**
     * 哔哩哔哩专用节点。
     */
    const val BILIBILI = "bilibili"

    val allSpecs: Map<String, ActionCategorySpec> = listOf(
        ActionCategorySpec(FLOW, "流程控制", "控制工作流的启动、结束、延迟、子流程及异常处理"),
        ActionCategorySpec(CONTROL, "条件分支", "基于条件表达式或匹配规则进行逻辑分支流转"),
        ActionCategorySpec(LOOP, "循环迭代", "提供 While、For-Each 循环与跳出控制"),
        ActionCategorySpec(DATA, "基础数据", "变量定义、赋值与基础数据合并处理"),
        ActionCategorySpec(TEXT, "文本处理", "字符串拼接、模板渲染、正则提取与替换"),
        ActionCategorySpec(MATH, "数值计算", "算术运算、比较及数学函数"),
        ActionCategorySpec(ARRAY, "数组处理", "列表与数组的过滤、映射、切片及聚合"),
        ActionCategorySpec(OBJECT, "对象操作", "JSON 对象的属性读取、设置与合并"),
        ActionCategorySpec(JSON, "JSON 转换", "结构化 JSON 对象的序列化与解析"),
        ActionCategorySpec(URL, "URL 处理", "URL 链接解析、参数拼装与编码"),
        ActionCategorySpec(CSV, "CSV 转换", "CSV 文本与行列表格数据的相互转换"),
        ActionCategorySpec(DATE, "日期时间", "时间戳、日期解析、格式化及时间计算"),
        ActionCategorySpec(CODEC, "文本编解码", "Base64、Hex、URL 与各种文本编码格式转换"),
        ActionCategorySpec(CRYPTO, "加密哈希", "MD5、SHA、AES 等摘要与加密解密处理"),
        ActionCategorySpec(HTTP, "网络请求", "发起 HTTP 请求并处理响应内容与请求头"),
        ActionCategorySpec(STORAGE, "私有存储", "工作流专用的键值、文件与持久化沙盒存储"),
        ActionCategorySpec(HTML, "HTML 解析", "HTML 文档解析与 CSS 选择器抽取"),
        ActionCategorySpec(XML, "XML 解析", "XML 文档解析与 XPath 节点查询"),
        ActionCategorySpec(ACTION, "系统动作", "调用宿主平台提供的原生系统能力与界面交互"),
        ActionCategorySpec(BILIBILI, "哔哩哔哩", "哔哩哔哩开放 API 与视频数据扩展"),
    ).associateBy { it.id }

    fun specOf(id: String): ActionCategorySpec =
        allSpecs[id] ?: ActionCategorySpec(id = id, label = id)
}
