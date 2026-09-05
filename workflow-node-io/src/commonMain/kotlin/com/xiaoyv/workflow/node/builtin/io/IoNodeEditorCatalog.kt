package com.xiaoyv.workflow.node.builtin.io

import com.xiaoyv.workflow.model.spec.ActionClipboardConfigKey
import com.xiaoyv.workflow.model.spec.ActionConfirmConfigKey
import com.xiaoyv.workflow.model.spec.ActionFileConfigKey
import com.xiaoyv.workflow.model.spec.ActionHttpBodyType
import com.xiaoyv.workflow.model.spec.ActionHttpConfigKey
import com.xiaoyv.workflow.model.spec.ActionHttpMethod
import com.xiaoyv.workflow.model.spec.ActionImagePreviewConfigKey
import com.xiaoyv.workflow.model.spec.ActionInputDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionNotificationConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenAppConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenUrlConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenWebConfigKey
import com.xiaoyv.workflow.model.spec.ActionProgressDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionProgressDialogMode
import com.xiaoyv.workflow.model.spec.ActionSelectDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionSelectOutputMode
import com.xiaoyv.workflow.model.spec.ActionShareConfigKey
import com.xiaoyv.workflow.model.spec.ActionStorageConfigKey
import com.xiaoyv.workflow.model.spec.ActionSyncCookieConfigKey
import com.xiaoyv.workflow.model.spec.ActionToastConfigKey
import com.xiaoyv.workflow.model.spec.ActionVideoPreviewConfigKey
import com.xiaoyv.workflow.node.core.ActionConfigFieldSpec
import com.xiaoyv.workflow.node.core.ActionEditorFieldKind
import com.xiaoyv.workflow.node.core.ActionEditorFieldOption
import com.xiaoyv.workflow.node.core.ActionNodeEditorSpec
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

/**
 * HTTP、文件、存储与系统副作用节点的集中编辑器说明目录。
 */
internal object IoNodeEditorCatalog {
    // ==================== HTTP 网络请求 ====================

    val httpRequest =
        ActionNodeEditorSpec(
            title = "HTTP 网络请求",
            description = "发送 HTTP/HTTPS 网络请求（GET, POST, PUT, DELETE 等），并获取响应状态、响应头及响应体。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.URL,
                        label = "请求 URL",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://api.example.com/v1/data",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.METHOD,
                        label = "请求方法 (Method)",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionHttpMethod.GET),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.GET), "GET"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.POST), "POST"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.PUT), "PUT"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.DELETE), "DELETE"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.PATCH), "PATCH"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpMethod.HEAD), "HEAD"),
                            ),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.HEADERS,
                        label = "请求头 (Headers - JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "{\"Authorization\": \"Bearer token\"}",
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.QUERY,
                        label = "查询参数 (Query - JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "{\"page\": 1, \"limit\": 20}",
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.BODY,
                        label = "请求体 (Body)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        placeholder = "输入 JSON 对象或字符串",
                        order = 4,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.BODY_TYPE,
                        label = "请求体类型 (Body Type)",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionHttpBodyType.JSON),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpBodyType.JSON), "JSON 格式 (application/json)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpBodyType.TEXT), "纯文本格式 (text/plain)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionHttpBodyType.FORM_URL_ENCODED), "表单编码 (application/x-www-form-urlencoded)"),
                            ),
                        order = 5,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.TIMEOUT_MILLIS,
                        label = "超时时间 (毫秒)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(15000),
                        order = 6,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.RETRY_COUNT,
                        label = "失败重试次数",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(0),
                        order = 7,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.USE_LOCAL_COOKIE_STORAGE,
                        label = "使用应用本地 Cookie 存储",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 8,
                    ),
                ),
        )

    val httpDownload =
        ActionNodeEditorSpec(
            title = "HTTP 文件下载",
            description = "从指定 URL 下载文件并保存至工作流沙箱目录中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.URL,
                        label = "下载文件 URL",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://example.com/file.zip",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.PATH,
                        label = "保存目录相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("downloads"),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.FILE_NAME,
                        label = "自定义保存文件名 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "默认按响应头或 URL 自动推断",
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.HEADERS,
                        label = "请求头 (Headers - JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionHttpConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("downloadResult"),
                        order = 4,
                    ),
                ),
        )

    // ==================== 文件沙箱操作 ====================

    val fileReadText =
        ActionNodeEditorSpec(
            title = "读取文本文件",
            description = "从工作流沙箱指定路径读取文本文件的完整内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "文件相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：data/config.json",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("fileContent"),
                        order = 1,
                    ),
                ),
        )

    val fileWriteText =
        ActionNodeEditorSpec(
            title = "写入文本文件",
            description = "将文本内容写入工作流沙箱指定路径（支持覆盖或追加模式）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "文件相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：logs/output.txt",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.TEXT,
                        label = "写入文本内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.APPEND,
                        label = "是否追加写入 (Append)",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("writeSuccess"),
                        order = 3,
                    ),
                ),
        )

    val fileCreate =
        ActionNodeEditorSpec(
            title = "创建空文件",
            description = "在工作流沙箱中创建指定路径的空文件。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "文件相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：temp/placeholder.txt",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("created"),
                        order = 1,
                    ),
                ),
        )

    val fileGetWorkingDirectory =
        ActionNodeEditorSpec(
            title = "获取工作目录路径",
            description = "获取当前工作流沙箱在本地设备文件系统上的绝对路径根目录。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("workingDir"),
                        order = 0,
                    ),
                ),
        )

    val fileDelete =
        ActionNodeEditorSpec(
            title = "删除文件或目录",
            description = "删除工作流沙箱中指定路径的文件或目录文件夹。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "文件/目录相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：temp/cache.json",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("deleted"),
                        order = 1,
                    ),
                ),
        )

    val fileExists =
        ActionNodeEditorSpec(
            title = "检查文件是否存在",
            description = "判断工作流沙箱中指定路径的文件或目录是否存在（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "文件/目录相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：data/db.sqlite",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("exists"),
                        order = 1,
                    ),
                ),
        )

    val fileMkdir =
        ActionNodeEditorSpec(
            title = "创建文件夹目录",
            description = "在工作流沙箱中递归创建多级目录文件夹。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "目录相对路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：output/images/thumbnails",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("mkdirSuccess"),
                        order = 1,
                    ),
                ),
        )

    val fileList =
        ActionNodeEditorSpec(
            title = "列出目录文件列表",
            description = "列出工作流沙箱指定目录下包含的所有子文件及子文件夹名称数组。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATH,
                        label = "目录相对路径 (留空为根目录)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive(""),
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("fileList"),
                        order = 1,
                    ),
                ),
        )

    val fileCopy =
        ActionNodeEditorSpec(
            title = "复制文件/目录",
            description = "将工作流沙箱中的文件或目录复制到指定目标路径。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.FROM_PATH,
                        label = "源文件路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：raw/sample.txt",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.TO_PATH,
                        label = "目标复制路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：backup/sample_bak.txt",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("copySuccess"),
                        order = 2,
                    ),
                ),
        )

    val fileMove =
        ActionNodeEditorSpec(
            title = "移动/重命名文件",
            description = "将工作流沙箱中的文件或目录移动或重命名到新路径。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.FROM_PATH,
                        label = "源文件路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：temp/draft.md",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.TO_PATH,
                        label = "目标移动路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：docs/final.md",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("moveSuccess"),
                        order = 2,
                    ),
                ),
        )

    val fileCompressZip =
        ActionNodeEditorSpec(
            title = "压缩为 ZIP 压缩包",
            description = "将多个文件或目录压缩打包为一个 ZIP 格式压缩文件。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.PATHS,
                        label = "待压缩文件路径列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[\"file1.txt\", \"images/\"]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.TO_PATH,
                        label = "输出 ZIP 文件路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：archives/bundle.zip",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("zipSuccess"),
                        order = 2,
                    ),
                ),
        )

    val fileExtractZip =
        ActionNodeEditorSpec(
            title = "解压 ZIP 压缩包",
            description = "将指定的 ZIP 压缩包文件解压提取到目标沙箱目录中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.FROM_PATH,
                        label = "源 ZIP 文件路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：archives/bundle.zip",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.TO_PATH,
                        label = "解压目标目录路径",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：extracted/",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionFileConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("unzipSuccess"),
                        order = 2,
                    ),
                ),
        )

    // ==================== 本地存储 (Preferences) ====================

    val storageGet =
        ActionNodeEditorSpec(
            title = "读取本地存储 (Preferences)",
            description = "从工作流持久化键值存储中读取指定键名对应的数据值。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.KEY,
                        label = "存储键名 (Key)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：user_token",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("storedValue"),
                        order = 1,
                    ),
                ),
        )

    val storageSet =
        ActionNodeEditorSpec(
            title = "写入本地存储 (Preferences)",
            description = "将数据以指定键名持久化写入工作流专属的本地存储中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.KEY,
                        label = "存储键名 (Key)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：user_token",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.VALUE,
                        label = "存储值 (Value)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "输入静态值或变量表达式",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("savedValue"),
                        order = 2,
                    ),
                ),
        )

    val storageDelete =
        ActionNodeEditorSpec(
            title = "删除本地存储项",
            description = "从工作流本地持久化存储中删除指定键名的数据。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.KEY,
                        label = "存储键名 (Key)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：user_token",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("deleteSuccess"),
                        order = 1,
                    ),
                ),
        )

    val storageHas =
        ActionNodeEditorSpec(
            title = "检查存储键是否存在",
            description = "检查工作流本地存储中是否已存在指定键名（输出布尔值）。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.KEY,
                        label = "存储键名 (Key)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：user_token",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionStorageConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("hasKey"),
                        order = 1,
                    ),
                ),
        )

    val storageClear =
        ActionNodeEditorSpec(
            title = "清空所有本地存储",
            description = "清空当前工作流所有的本地持久化存储键值数据。",
            fields = persistentListOf(),
        )

    // ==================== 系统与 UI 副作用 ====================

    val openExternalUrl =
        ActionNodeEditorSpec(
            title = "系统浏览器打开 URL",
            description = "唤起设备系统默认浏览器打开指定的网页链接 URL。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionOpenUrlConfigKey.URL,
                        label = "网页 URL 地址",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://example.com",
                        order = 0,
                    ),
                ),
        )

    val openExternalApp =
        ActionNodeEditorSpec(
            title = "唤起第三方 App",
            description = "通过 Schema URI 协议调起外部第三方应用，支持失败时回退打开备用 URL。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionOpenAppConfigKey.URI,
                        label = "App Schema URI",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "bilibili://video/BV1xx411c7mD",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionOpenAppConfigKey.FALLBACK_URL,
                        label = "回退打开网页 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "https://www.bilibili.com",
                        order = 1,
                    ),
                ),
        )

    val openInternalWeb =
        ActionNodeEditorSpec(
            title = "内置 WebView 打开网页",
            description = "在应用内部内置 WebView 窗口中加载并浏览指定 URL。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionOpenWebConfigKey.URL,
                        label = "网页 URL 地址",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://m.bilibili.com",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionOpenWebConfigKey.HEADERS,
                        label = "自定义 Header (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 1,
                    ),
                ),
        )

    val showToast =
        ActionNodeEditorSpec(
            title = "显示 Toast 提示",
            description = "在设备屏幕下方弹出一条轻量级 Toast 提示气泡。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionToastConfigKey.MESSAGE,
                        label = "提示消息内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：操作成功！",
                        order = 0,
                    ),
                ),
        )

    val writeClipboard =
        ActionNodeEditorSpec(
            title = "写入系统剪贴板",
            description = "将指定文本内容复制到系统剪贴板中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionClipboardConfigKey.TEXT,
                        label = "复制文本内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 0,
                    ),
                ),
        )

    val readClipboard =
        ActionNodeEditorSpec(
            title = "读取系统剪贴板",
            description = "读取当前系统剪贴板中的文本内容并保存到指定变量中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionClipboardConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("clipboardText"),
                        order = 0,
                    ),
                ),
        )

    val confirm =
        ActionNodeEditorSpec(
            title = "二次确认对话框 (Confirm)",
            description = "弹出模态确认对话框，等待用户点击确认或取消按钮后决定后续分支流程。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionConfirmConfigKey.TITLE,
                        label = "弹窗标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "例如：确认操作",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionConfirmConfigKey.MESSAGE,
                        label = "提示正文消息",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "例如：确定要执行该操作吗？",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionConfirmConfigKey.CONFIRM_TEXT,
                        label = "确认按钮文字",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive("确认"),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionConfirmConfigKey.CANCEL_TEXT,
                        label = "取消按钮文字",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        defaultValue = JsonPrimitive("取消"),
                        order = 3,
                    ),
                ),
        )

    val share =
        ActionNodeEditorSpec(
            title = "调用系统分享面板",
            description = "调起系统原生分享面板，分享文本或网页链接至其他应用。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionShareConfigKey.TITLE,
                        label = "分享弹窗标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionShareConfigKey.TEXT,
                        label = "分享文本内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionShareConfigKey.URL,
                        label = "分享网页链接 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 2,
                    ),
                ),
        )

    val notification =
        ActionNodeEditorSpec(
            title = "发送系统通知栏消息",
            description = "在设备顶部系统通知栏中发送一条附带标题与正文的消息通知。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionNotificationConfigKey.TITLE,
                        label = "通知标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "工作流通知",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionNotificationConfigKey.CONTENT,
                        label = "通知正文内容",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        order = 1,
                    ),
                ),
        )

    val vibrate =
        ActionNodeEditorSpec(
            title = "设备震动反馈",
            description = "触发设备震动马达提供物理触觉震动反馈。",
            fields = persistentListOf(),
        )

    val inputDialog =
        ActionNodeEditorSpec(
            title = "输入框对话框 (Input Dialog)",
            description = "弹出带文本输入框的模态对话框，获取用户手动输入的文本内容。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionInputDialogConfigKey.TITLE,
                        label = "对话框标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "请输入内容",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionInputDialogConfigKey.SUBTITLE,
                        label = "提示说明文字",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionInputDialogConfigKey.DEFAULT_VALUE,
                        label = "输入框初始默认值",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionInputDialogConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("userInput"),
                        order = 3,
                    ),
                ),
        )

    val progressDialog =
        ActionNodeEditorSpec(
            title = "显示进度条对话框",
            description = "显示一个模态进度加载对话框，支持不确定进度与百分比精确进度模式。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.TITLE,
                        label = "任务标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "处理中...",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.MESSAGE,
                        label = "进度说明文本",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "正在下载资源...",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.MODE,
                        label = "进度显示模式",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionProgressDialogMode.INDETERMINATE),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionProgressDialogMode.INDETERMINATE), "不确定进度 (旋转动画)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionProgressDialogMode.DETERMINATE), "精确进度 (百分比数值)"),
                            ),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.PROGRESS,
                        label = "当前进度数值",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(0),
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.MAX_PROGRESS,
                        label = "最大进度总值",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(100),
                        order = 4,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.TASK_ID,
                        label = "进度任务唯一 ID (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 5,
                    ),
                ),
        )

    val progressUpdate =
        ActionNodeEditorSpec(
            title = "更新进度条进度",
            description = "动态更新已有进度对话框的当前进度百分比与说明文案。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.PROGRESS,
                        label = "当前最新进度值",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.MAX_PROGRESS,
                        label = "最大进度总值 (可选)",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.MESSAGE,
                        label = "更新说明文本 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.TITLE,
                        label = "更新标题 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.TASK_ID,
                        label = "进度任务唯一 ID (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 4,
                    ),
                ),
        )

    val progressDismiss =
        ActionNodeEditorSpec(
            title = "关闭进度条对话框",
            description = "主动关闭并隐藏当前正在显示的进度条对话框。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionProgressDialogConfigKey.TASK_ID,
                        label = "关闭指定任务 ID (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 0,
                    ),
                ),
        )

    val selectDialog =
        ActionNodeEditorSpec(
            title = "列表选择对话框",
            description = "弹出模态列表选择对话框，支持单选或多选模式并输出所选值或索引。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionSelectDialogConfigKey.TITLE,
                        label = "选择对话框标题",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        placeholder = "请选择",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSelectDialogConfigKey.OPTIONS,
                        label = "选项列表 (JSON 数组)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[{\"title\": \"选项A\", \"value\": \"a\"}]",
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSelectDialogConfigKey.IS_MULTI_SELECT,
                        label = "是否支持多选",
                        kind = ActionEditorFieldKind.BOOLEAN,
                        required = false,
                        defaultValue = JsonPrimitive(false),
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSelectDialogConfigKey.OUTPUT_MODE,
                        label = "输出模式 (Output Mode)",
                        kind = ActionEditorFieldKind.SELECT,
                        required = false,
                        defaultValue = JsonPrimitive(ActionSelectOutputMode.VALUE),
                        options =
                            persistentListOf(
                                ActionEditorFieldOption(JsonPrimitive(ActionSelectOutputMode.VALUE), "输出选中值 (Value)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionSelectOutputMode.INDEX), "输出选中下标 (Index)"),
                                ActionEditorFieldOption(JsonPrimitive(ActionSelectOutputMode.BOTH), "同时输出值与下标 (Both)"),
                            ),
                        order = 3,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSelectDialogConfigKey.OUTPUT_KEY,
                        label = "输出字段名",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        defaultValue = JsonPrimitive("selectedOption"),
                        order = 4,
                    ),
                ),
        )

    val imagePreview =
        ActionNodeEditorSpec(
            title = "大图预览画廊",
            description = "调起全屏大图预览画廊，支持多图左右滑动查看与缩放。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionImagePreviewConfigKey.IMAGES,
                        label = "图片 URL 列表 (JSON 数组或单个 URL)",
                        kind = ActionEditorFieldKind.JSON,
                        required = true,
                        placeholder = "[\"https://example.com/1.jpg\"]",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionImagePreviewConfigKey.INDEX,
                        label = "初始展示图片索引",
                        kind = ActionEditorFieldKind.NUMBER,
                        required = false,
                        defaultValue = JsonPrimitive(0),
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionImagePreviewConfigKey.HEADERS,
                        label = "图片加载 Header (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 2,
                    ),
                ),
        )

    val videoPreview =
        ActionNodeEditorSpec(
            title = "全屏视频播放预览",
            description = "唤起内置全屏视频播放器流式播放指定的在线或本地视频。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionVideoPreviewConfigKey.URL,
                        label = "视频流 / 文件 URL",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://example.com/video.mp4",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionVideoPreviewConfigKey.HEADERS,
                        label = "请求 Header (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 1,
                    ),
                ),
        )

    val syncCookie =
        ActionNodeEditorSpec(
            title = "同步 Web 页面 Cookie",
            description = "静默打开目标网页并提取登录 Cookie 同步至工作流上下文中。",
            fields =
                persistentListOf(
                    ActionConfigFieldSpec(
                        key = ActionSyncCookieConfigKey.URL,
                        label = "网页 URL 地址",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = true,
                        placeholder = "https://passport.bilibili.com/login",
                        order = 0,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSyncCookieConfigKey.TITLE,
                        label = "页面标题 (可选)",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 1,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSyncCookieConfigKey.USER_AGENT,
                        label = "自定义 User-Agent",
                        kind = ActionEditorFieldKind.TEMPLATE_TEXT,
                        required = false,
                        order = 2,
                    ),
                    ActionConfigFieldSpec(
                        key = ActionSyncCookieConfigKey.HEADERS,
                        label = "请求 Header (JSON 对象)",
                        kind = ActionEditorFieldKind.JSON,
                        required = false,
                        order = 3,
                    ),
                ),
        )
}
