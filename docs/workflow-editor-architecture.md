# 工作流 Web 编辑器架构

## 1. 目标与边界

本项目的工作流运行时已经具备节点、端口、边、布局坐标、迁移和拓扑校验能力。编辑器不应复制这些规则，更不应维护一套独立的节点字典。目标是让**节点定义成为唯一事实来源**
：运行时和任意编辑器均从同一份声明式规格得到所需信息。

编辑器是可离线部署的 HTML 应用。只要获得“编辑器 Manifest”和工作流 JSON，它就可以在浏览器、大屏、桌面浏览器或嵌入式 WebView 中完整渲染、编辑并导出工作流。浏览器本身不执行工作流，也不处理本机文件、网络、系统能力或
`ActionSideEffect`。

本地 HTTP 服务是可选的设备调试桥接层：设备连接后自动导出 Manifest 与工作流，接收编辑结果，在设备侧校验/保存/运行，并将日志和运行状态回推给编辑器。设备脱机时，编辑器仍可通过导入、导出 JSON
正常工作。

### 非目标

- 不将 React、HTML、HTTP、WebSocket 等依赖引入 `:workflow-core`。
- 不让浏览器成为执行引擎或 SideEffect 宿主。
- 不以 Kuiver 作为可编辑工作流的底层模型；它可保留为 Compose 端的只读预览。
- 不信任浏览器做最终校验；任何可保存或可运行的工作流必须由设备侧核心校验。

## 2. 总体拓扑

```text
                   节点模块
  executor + ActionNodeSpec + ActionNodeEditorSpec
                          │
                          ▼
              ActionNodeRegistry / Manifest exporter
                          │
          ┌───────────────┴────────────────┐
          ▼                                ▼
  文件、二维码、接口导出              可选 Device Bridge
  EditorManifest + Workflow JSON      HTTP + WebSocket
          │                                │
          └───────────────┬────────────────┘
                          ▼
                 HTML 编辑器（离线静态应用）
          节点画布 / 端口连线 / 表单 / 日志面板
                          │
               导出工作流 JSON 或发起保存/运行请求
                          │
                          ▼
                    连接设备（唯一执行端）
             migrate → validate → execute → SideEffect
```

## 3. 统一节点描述：Spec 与 Editor Spec

### 3.1 现状与缺口

现有 `ActionNodeSpec` 已包含 `type`、`category`、端口、必填配置和权限，足以供执行器和 `ActionWorkflowValidator` 校验连接是否合法。但编辑器无法从 `requiredConfigKeys: Set<String>`
推导以下信息：字段标签、字段控件、默认值、枚举选项、提示、分组、敏感字段、端口的显示名称与位置。

这些信息不应由 Web 前端维护。否则每新增一个节点都必须同时修改 Kotlin 执行节点和前端节点字典，最终一定漂移。

### 3.2 推荐的数据模型

在 `:workflow-core` 中增加**纯 Kotlin、可序列化、无 UI 依赖**的编辑器描述模型。推荐将其作为 `ActionNodeSpec.editor` 的可选字段；没有编辑器描述的节点仍可执行，但 Manifest
将标识它不可由通用编辑器创建或配置。

```kotlin
@Serializable
data class ActionNodeSpec(
    val type: String,
    val latestVersion: Int = 1,
    val category: String,
    val inputPorts: SerializeList<ActionPortSpec> = persistentListOf(),
    val outputPorts: SerializeList<ActionPortSpec> = persistentListOf(),
    val requiredConfigKeys: Set<String> = emptySet(),
    val requiredCapabilities: Set<String> = emptySet(),
    val editor: ActionNodeEditorSpec? = null,
)

@Serializable
data class ActionNodeEditorSpec(
    val title: String,
    val description: String = "",
    val icon: String? = null,
    val color: String? = null,
    val defaultConfig: JsonObject = JsonObject(emptyMap()),
    val fields: SerializeList<ActionConfigFieldSpec> = persistentListOf(),
    val creatable: Boolean = true,
)
```

`ActionPortSpec` 同时补充表现层可用但不影响运行时语义的字段：

```kotlin
@Serializable
data class ActionPortSpec(
    val id: String,
    val kind: String = ActionPortKind.CONTROL,
    val direction: String,
    val maxConnections: Int = UNLIMITED,
    val label: String = id,
    val order: Int = 0,
    val color: String? = null,
    val editorVisible: Boolean = true,
)
```

`ActionConfigFieldSpec` 必须描述 JSON 值，而不是具体 Compose 或 React 组件。建议包含：

- `key`：与 `ActionNode.config` 完全一致的稳定键。
- `label`、`description`、`group`、`order`。
- `kind`：`text`、`template-text`、`textarea`、`number`、`boolean`、`select`、`json`、`string-list`、`key-value-list`、`secret-reference` 等。
- `defaultValue`、`placeholder`、`options`、`required`、`readOnly`、`sensitive`。
- `visibleWhen`、`enabledWhen`：基于当前 config 的简单声明式条件；不要在 Manifest 中传递可执行脚本。
- `validation`：最小/最大值、正则、长度、枚举；复杂拓扑规则仍归 `ActionWorkflowValidator`。

运行时应保留 `requiredConfigKeys` 的强校验。构建期或单元测试必须验证：每个 required key 都有对应 `fields` 描述或被明确标记为 `editorHidden`；字段默认值必须可被当前节点执行器接受。

### 3.3 单一来源的节点定义示例

```kotlin
ActionNodeDefinition(
    spec = ActionNodeSpec(
        type = ActionNodeType.FLOW_DELAY,
        category = ActionNodeCategory.FLOW,
        inputPorts = persistentListOf(inPort),
        outputPorts = persistentListOf(nextPort),
        requiredConfigKeys = setOf(ActionFlowConfigKey.DELAY_MILLIS),
        editor = ActionNodeEditorSpec(
            title = "延时",
            description = "暂停当前分支指定时长",
            icon = "timer",
            color = "#6366F1",
            defaultConfig = buildJsonObject {
                put(ActionFlowConfigKey.DELAY_MILLIS, 1_000)
            },
            fields = persistentListOf(
                ActionConfigFieldSpec.number(
                    key = ActionFlowConfigKey.DELAY_MILLIS,
                    label = "延时（毫秒）",
                    minimum = 0,
                    required = true,
                ),
            ),
        ),
    ),
    executor = { node, _ -> /* 现有执行逻辑 */ },
)
```

节点模块的维护者只维护这一个定义。Manifest、节点调色板、卡片标题、端口、配置表单、默认节点实例和服务端校验都由它派生。

## 4. Editor Manifest

### 4.1 导出原则

渲染前置数据的导出应由 `:workflow-core` 的 `codec` 包负责，而不是由 Device Bridge 或前端拼装。推荐新增 `ActionEditorManifestCodec(json, registry)`：它从 `ActionNodeRegistry`
导出一个带版本号、只含描述性数据的 DTO，不能导出 executor、能力实现、文件路径、密钥或任意 JVM/Kotlin 对象。

```kotlin
@Serializable
data class ActionEditorManifest(
    val schemaVersion: Int,
    val workflowFormatVersion: Int,
    val generatedAt: String? = null,
    val nodeTypes: List<ActionEditorNodeManifest>,
    val categories: List<ActionEditorCategoryManifest>,
)
```

Manifest 至少包含：

- 节点稳定类型、当前节点版本、分类、标题、描述、图标、颜色。
- 输入/输出端口的 ID、名称、kind、方向、最大连接数和显示顺序。
- 默认 `config` 和全部字段描述。
- 节点所需能力，以及当前设备是否可用的状态；能力可用性是设备信息，建议作为单独 `DeviceCapabilities` 消息覆盖，避免把同一 Manifest 缓存错误地绑定到某台设备。
- `schemaVersion` 与节点版本，用于浏览器兼容性检查。

编辑器应允许从文件读取 Manifest；本地服务只是另一个 Manifest 来源。浏览器可以缓存 Manifest，缓存键由 `schemaVersion` 和节点类型/版本集合构成。

### 4.1.1 Codec 职责

三个 Codec 的职责必须分开：

| Codec                            | 输入/来源                       | 输出                          | 职责                    |
|----------------------------------|-----------------------------|-----------------------------|-----------------------|
| `ActionWorkflowCodec`（已有）        | `ActionWorkflow` / 工作流 JSON | 工作流 JSON / 导入结果             | 导入、格式迁移、节点迁移和工作流校验    |
| `ActionEditorManifestCodec`（新增）  | `ActionNodeRegistry`        | `ActionEditorManifest` JSON | 导出当前运行时已注册节点的完整渲染前置数据 |
| `ActionEditorBundleCodec`（新增，可选） | Manifest + Workflow         | 单个离线编辑包 JSON                | 供文件分享、二维码或浏览器离线打开使用   |

`ActionWorkflowCodec` 不应承担 Manifest 导出：工作流是用户文档，需要迁移和校验；Manifest 是由当前节点注册表派生的静态能力目录，两者版本、缓存和信任边界都不同。`ActionEditorBundleCodec`
只负责打包，不改变两者语义。

```kotlin
class ActionEditorManifestCodec(
    private val json: Json,
    private val registry: ActionNodeRegistry,
) {
    fun export(): String = json.encodeToString(
        ActionEditorManifest.serializer(),
        registry.toEditorManifest(),
    )
}

class ActionEditorBundleCodec(private val json: Json) {
    fun export(manifest: ActionEditorManifest, workflow: ActionWorkflow): String
    fun import(raw: String): ActionEditorBundle
}
```

设备端导入工作流时永远使用自身 `ActionWorkflowCodec` 和自身注册表；外部 Bundle 携带的 Manifest 仅用于离线渲染和兼容性提示，不能替代设备侧 Manifest 或授权未知节点执行。

### 4.2 工作流文档

工作流仍使用现有 `ActionWorkflow` JSON。其 `ActionNode.layout` 是画布坐标的唯一持久化来源；不要额外维护 React Flow 专用位置文件。浏览器可以临时维护 viewport、选择态、对齐辅助线等 UI
状态，但不应混入可执行工作流文档。

建议仅在有明确产品价值时，以 `ActionWorkflow.editorMetadata` 添加可选、前向兼容的持久化信息，例如画布 viewport、分组和注释。它不能参与运行时调度或改变节点执行语义。

## 5. Web 编辑器实现

### 5.1 技术选型

`workflow-editor-web` 当前是零构建步骤的静态 HTML/CSS/JavaScript 应用，便于直接复制到浏览器、大屏或设备 Bridge 托管目录。它以通用节点卡片和 `Editor Manifest`
渲染，不维护节点类型专属组件；支持节点拖动、输出端口拖至输入端口的连线、连接数/类型预校验、表单编辑、导入导出以及设备保存/运行/日志。

画布提供水平、垂直和紧凑三种自动布局。用户在布局下拉框中切换类型时立即重新排版；导入工作流也始终使用当前选中的布局类型，避免沿用其它设备或编辑器保存的坐标。紧凑布局通过多轮重心排序得到拓扑顺序，再按列填充为二维网格，避免长工作流始终排成单行；关联节点会尽可能相邻，以降低边的长度与交叉概率。

如果后续需要平移缩放、框选、缩略图或高级自动布局，可在不改变 `ActionWorkflow`、Manifest 或 Bridge 协议的前提下替换画布实现为 React Flow/XYFlow。Kuiver 保留为 Compose 端只读预览。

Kuiver 保留给 Compose Demo 的只读图预览即可。它的图模型以节点对边为中心，当前接入还需合并相同起止节点的边，无法忠实表达本项目的端口级 `ActionEdge`。

### 5.2 前端状态

前端状态分为三类，禁止混用：

| 状态                      | 归属        | 是否导出     |
|-------------------------|-----------|----------|
| Manifest                | 外部输入，只读   | 可独立缓存/导入 |
| ActionWorkflow          | 编辑文档      | 是        |
| viewport、选择、拖拽预览、菜单打开状态 | 浏览器 UI 状态 | 否        |

连线创建流程：浏览器根据 Manifest 做端口方向、kind 与连接数的预检查；提交后修改 `ActionWorkflow.edges`；连接设备时由设备调用 `ActionWorkflowValidator`
进行最终裁决。删除节点时，前端必须同时删除所有关联边，并将工作流入口、全局错误节点等引用交给服务端校验。

从端口拖到空白处时，前端按可兼容的输入/输出端口过滤节点调色板；选中后以 `defaultConfig` 创建 `ActionNode`、写入当前位置并自动生成边。节点 ID 由浏览器产生 UUID，或由连接设备分配；建议统一用
UUID，服务端只验证唯一性。

### 5.3 表单与模板

字段 `kind` 映射到通用表单控件。`template-text` 与普通 `text` 不同：前者需要提示 `${vars.xxx}` 语法，但原始值仍按字符串写入 `config`。JSON 字段必须在编辑时解析并显示错误，保存时保持
`JsonElement` 的准确类型，不能把数字、布尔值全部降为字符串。

`secret-reference` 只保存密钥名称或引用 ID，绝不通过 Manifest、工作流导出或日志推送真实凭据。

## 6. 可选 Device Bridge

### 6.1 模块划分

```text
:workflow-core
  ActionWorkflow、ActionNodeSpec、Editor Spec、Manifest exporter、迁移、校验

:workflow-node-*
  每种节点的 executor 与同源 Spec/Editor Spec

:workflow-editor-web
  静态 HTML/JS/CSS；可独立部署，也可被设备服务托管

:workflow-editor-bridge-contract
  HTTP/WebSocket 的序列化 DTO 与协议版本；依赖 workflow-core

:workflow-editor-bridge-jvm
  Ktor 服务、静态资源托管、会话、运行事件转发；供 desktopApp 使用

:workflow-editor-bridge-android（可选）
  Android 服务封装与前台服务生命周期；复用 contract 和桥接业务逻辑

:workflow-editor-mcp（可选）
  面向 AI 客户端的 MCP 适配层；将 Manifest、架构资料、校验、保存和日志能力暴露为
  MCP resources/tools，不包含工作流执行器或第二套节点规则

:workflow-editor-mcp-jvm（可选）
  JVM 组装适配层；将 MCP 的显式运行工具连接至 JVM Bridge 的运行控制器。
  该模块依赖 bridge-jvm 与 mcp，避免反向让 Bridge 依赖 MCP

:shared
  现有 Compose Demo；按需连接 Bridge，不承载编辑器业务
```

不要把 Ktor Server 放入 `commonMain` 并假设所有目标都能可靠监听端口。Desktop JVM 最适合作为首个 Bridge；Android 若开启局域网调试，应由前台服务负责生命周期。iOS 可以作为浏览器客户端或使用远端/桌面设备的
Bridge，是否提供本地监听另行评估。

### 6.2 接口

静态 HTML 与 API 同源托管时无需 CORS。离线编辑器也可以从任意静态服务器打开，再通过用户输入设备地址连接 Bridge。

| 方法     | 路径                            | 用途                          |
|--------|-------------------------------|-----------------------------|
| `GET`  | `/api/v1/manifest`            | 导出当前设备已注册节点的 Manifest       |
| `GET`  | `/api/v1/workflows/{id}`      | 获取工作流和 `revision`           |
| `PUT`  | `/api/v1/workflows/{id}`      | 保存完整工作流，必须携带 `baseRevision` |
| `POST` | `/api/v1/workflows/validate`  | 迁移并校验但不保存                   |
| `POST` | `/api/v1/workflows/{id}/runs` | 请求设备用已保存版本执行                |
| `GET`  | `/api/v1/runs/{runId}`        | 获取运行状态和有界、脱敏事件历史，供编辑器断线重连   |
| `POST` | `/api/v1/runs/{runId}/cancel` | 取消设备侧运行                     |
| `GET`  | `/api/v1/device`              | 名称、版本、可用能力、Bridge 状态        |
| `WS`   | `/api/v1/events`              | 日志、节点状态、保存结果、运行事件           |

保存操作推荐全量文档加乐观锁，而不是第一版就实现复杂 Patch：

```json
{
  "baseRevision": 12,
  "workflow": { "formatVersion": 1, "nodes": [], "edges": [] }
}
```

如果版本不一致，设备返回 `409 Conflict` 和当前 revision；浏览器提示刷新、比较或另存副本。后续需要多人协作时，再在同一 contract 上引入 Patch/OT/CRDT，不要提前污染核心工作流模型。

### 6.3 运行与日志

浏览器的“运行”按钮只是向设备发送请求。Bridge 在设备侧：

1. 读取指定 revision 的工作流快照。
2. `registry.migrate()` 后执行 `ActionWorkflowValidator.validate()`。
3. 校验通过才启动 `ActionWorkflowEngine`。
4. 将 `ActionExecutionEvent` 转换为协议事件，经 WebSocket 推送。
5. 设备自己的 Compose SideEffect 宿主处理 `ActionSideEffect`；浏览器仅接收状态和日志，不接收操作权。

每个事件必须带 `runId`、序号、时间和节点 ID。浏览器断线不应取消运行；重新连接后可按 `runId` 拉取或订阅最新日志。运行中的节点、成功、失败、跳过状态可叠加到画布卡片上。

## 7. AI MCP 适配层

### 7.1 职责

`workflow-editor-mcp` 让 AI 客户端在不阅读 Kotlin 源码的前提下理解当前设备实际支持的节点、端口、配置字段、架构约束和已有工作流，并据此生成可校验的 `ActionWorkflow` JSON。

MCP 服务本身不调用模型，也不实现“自然语言 → 工作流”的第二套生成器。正确的责任划分是：AI 客户端读取 MCP 资源与工具结果，在自身推理中生成候选 JSON；MCP 再把 JSON 交给核心的
`ActionWorkflowCodec`、`ActionWorkflowValidator` 和设备运行时。这保证节点语义和校验逻辑仍只有一份。

```text
用户需求 → AI 客户端
              │  读取 Manifest / 架构 / 模板
              ▼
         候选 ActionWorkflow JSON
              │  validate_workflow
              ▼
   workflow-editor-mcp → ActionWorkflowCodec / Validator
              │
     校验问题或可保存的规范工作流
```

### 7.2 模块与传输

该模块依赖 `:workflow-core` 与 `:workflow-editor-bridge-contract`；目前以轻量的 newline JSON-RPC stdio 适配实现 MCP 协议，不依赖 React 前端。它复用 `ActionEditorManifestCodec`、
`ActionWorkflowCodec` 和 Bridge 的工作流仓储/运行日志服务，禁止复制节点元数据或校验规则。

- **本地 AI 客户端**：优先提供 MCP stdio 入口，适合 Codex、IDE 等本机调用者，且无需开放端口。
- **已启用 Device Bridge 时**：可选提供受认证保护的 Streamable HTTP MCP 入口；它应与浏览器 Bridge 使用独立 token 和权限范围。
- **只读模式**：可在没有设备执行授权时提供 Manifest、架构、工作流读取和校验，适合仅生成文件。

### 7.3 MCP Resources

资源用于给 AI 提供稳定、可缓存的上下文。不要让 AI 每次都读取全部 Kotlin 源码，也不应把数百个节点的完整 Manifest 强塞进每次提示。

| Resource URI                       | 内容                                          |
|------------------------------------|---------------------------------------------|
| `workflow://architecture`          | 本文档的机器可读摘要：模型、执行边界、并发/循环/SideEffect 约束、版本规则 |
| `workflow://manifest`              | 全量 `ActionEditorManifest`；适用于首次同步或客户端缓存     |
| `workflow://categories/{category}` | 指定分类的节点摘要，含类型、标题、端口和描述                      |
| `workflow://nodes/{type}`          | 单个节点完整 Editor Spec、默认 config、端口与字段说明        |
| `workflow://templates`             | 已验证的示例工作流及适用场景                              |
| `workflow://workflows/{id}`        | 已保存工作流的 JSON、revision 与最后校验结果               |
| `workflow://runs/{runId}/logs`     | 指定运行的脱敏日志和节点状态                              |

`workflow://architecture` 必须明确：AI 只能生成 JSON，真正的执行、副作用和能力授权均发生在设备端；数据边目前若仍不受运行时支持，也必须写入该资源，避免 AI 生成不可执行的 `data` 边。

### 7.4 MCP Tools

| Tool                | 权限 | 行为                               |
|---------------------|----|----------------------------------|
| `list_node_types`   | 只读 | 按分类、关键词或能力列出节点摘要                 |
| `get_node_spec`     | 只读 | 获取指定节点的完整编辑器规格                   |
| `get_workflow`      | 只读 | 获取工作流及 revision                  |
| `validate_workflow` | 只读 | 迁移并校验候选 JSON，返回结构化 issue；不保存、不执行 |
| `create_workflow`   | 写入 | 用候选 JSON 新建工作流；服务端迁移和校验通过后才保存    |
| `update_workflow`   | 写入 | 基于 `baseRevision` 保存完整工作流，冲突时拒绝  |
| `run_workflow`      | 执行 | 明确请求设备以某个 revision 运行            |
| `cancel_run`        | 执行 | 取消指定设备侧运行                        |
| `get_run_status`    | 只读 | 获取运行状态和最新事件                      |

实现同时保留早期 `workflow_*`/`run_*` 工具名作为兼容别名；新客户端应使用上表中的规范名称。

生成流程应固定为：`list_node_types` / `get_node_spec` → AI 生成 JSON → `validate_workflow` → AI 按 issue 修正 → 用户确认后 `create_workflow` 或 `update_workflow`。`run_workflow`
必须只在用户明确要求运行或测试时调用，不能由“生成工作流”隐式触发。

### 7.5 结构化校验返回

`validate_workflow` 返回现有 `ActionWorkflowValidation` 的稳定 DTO，而不是仅返回人类字符串。至少包含 `isValid`、迁移后的工作流、`code`、`message`、`nodeId`、`edgeId` 与字段路径。这样 AI
能定位端口、缺失配置、并发域或循环范围问题并自动修正。

工具返回的工作流、日志和配置必须遵守与 Web Bridge 相同的脱敏规则。`secret-reference` 可见其引用名称，真实值不可经 MCP 返回；涉及文件、网络、外部应用等能力的实际执行必须遵循设备侧授权策略。

## 8. 安全与运维

- 默认只绑定 `127.0.0.1`，只有用户明确启用“局域网编辑”时绑定 LAN 地址。
- 首次连接使用短期随机配对 token；二维码只包含设备地址、协议版本和 token，不包含密钥。
- 所有读写、运行、取消、WebSocket 与 MCP 工具均验证 token；浏览器和 MCP 使用可独立撤销的 token。
- 对运行接口限流；日志脱敏，尤其是 HTTP Header、Cookie、文件绝对路径和 `secret-reference` 解析结果。
- 工作流导出永远不包含运行期变量、SideEffect 返回值或真实凭据。
- Bridge 明确展示设备端口、监听范围、已连接浏览器和运行中的任务，并允许一键关闭。

## 9. 实施顺序

1. 在 `ActionNodeSpec`/`ActionPortSpec` 添加可序列化 Editor 描述，并为内置节点补齐标题、端口标签、默认值和字段定义。
2. 实现 `ActionNodeRegistry.exportEditorManifest()`、序列化测试，以及“Spec 与 Editor 字段覆盖关系”的单元测试。
3. 建立 `workflow-editor-web`：Manifest 导入、工作流 JSON 导入/导出、通用节点卡片、端口连线、配置表单和客户端预校验。
4. 建立 `workflow-editor-bridge-contract` 与 JVM Bridge：Manifest、工作流读取/保存、校验、WebSocket 日志。
5. 建立 `workflow-editor-mcp` 的只读 resources 和 `validate_workflow`；以 MCP 集成测试验证 AI 可据 Manifest 生成合法 JSON。
6. 接入设备侧运行和本机 SideEffect，给画布叠加实时节点状态；之后开放显式授权的 MCP `run_workflow`。
7. 最后增加 Android 前台服务、LAN 配对二维码、自动重连、自动保存和高级协作能力。

## 10. 验收标准

- 新增一个节点时，只修改该节点模块中的定义；无需修改 HTML 节点字典。
- 将 Manifest 和工作流 JSON 拷贝到离线浏览器，仍可创建、连接、配置并导出工作流。
- 浏览器创建的合法工作流可被现有 `ActionWorkflowCodec.import()` 迁移并校验。
- 浏览器的非法连线能即时提示；绕过前端提交的非法 JSON 仍会被设备端拒绝。
- 设备未运行 Bridge 时，编辑器无执行能力但不失去编辑能力。
- Bridge 连接后，浏览器可保存、请求设备执行并实时看到日志；所有副作用仍只在设备上发生。
- AI 可经 MCP 按需读取节点规格和架构资料，调用 `validate_workflow` 后生成可由 `ActionWorkflowCodec` 导入的 JSON；未获得用户明确请求时，AI 不会触发设备执行。
