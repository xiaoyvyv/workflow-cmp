<p align="center">
  <img src="docs/workflow_logo.svg" width="112" height="112" alt="workflow-cmp Logo" />
</p>

<h1 align="center">workflow-cmp</h1>

<p align="center">
  <strong>基于 Kotlin Multiplatform 与 Compose Multiplatform 的声明式 DAG 自动化与工作流编排执行引擎</strong>
</p>

<p align="center">
  专为跨平台自动化任务、数据管道与复杂业务流程编排而设计。<br />
  将流程控制、网络请求、网页解析、数据清洗转换与异步任务抽象为声明式节点协议，在 Android、iOS 与 Desktop (JVM) 实现多端统一调度执行。
</p>

<p align="center">
  <a href="#-可视化工作流编辑器-workflow-editor-web">可视化编辑器</a> •
  <a href="#1-项目架构与模块划分">项目架构</a> •
  <a href="#2-执行模型与调度内核">调度内核</a> •
  <a href="#3-模板与表达式引擎">表达式引擎</a> •
  <a href="#4-内置节点参考">节点字典</a> •
  <a href="#6-快速接入与示例">快速接入</a>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Version" /></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose_Multiplatform-1.12.0-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" /></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android_24+-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Support" /></a>
  <a href="https://developer.apple.com/ios/"><img src="https://img.shields.io/badge/Platform-iOS_Arm64-000000?style=flat-square&logo=apple&logoColor=white" alt="iOS Support" /></a>
  <a href="https://openjdk.org/"><img src="https://img.shields.io/badge/Platform-Desktop_JVM-EA4335?style=flat-square&logo=openjdk&logoColor=white" alt="Desktop JVM" /></a>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue?style=flat-square" alt="License" /></a>
  <a href="https://xiaoyvyv.github.io/workflow-cmp/"><img src="https://img.shields.io/badge/Online_Demo-GitHub_Pages-22C55E?style=flat-square&logo=githubpages&logoColor=white" alt="GitHub Pages Demo" /></a>
  <img src="https://img.shields.io/badge/Architecture-DAG_Orchestrator-8B5CF6?style=flat-square" alt="DAG Architecture" />
  <img src="https://img.shields.io/badge/Concurrency-Coroutines_%26_Flow-F59E0B?style=flat-square" alt="Coroutines and Flow" />
  <img src="https://img.shields.io/badge/Tests-Passing-brightgreen?style=flat-square" alt="Build & Tests" />
</p>

---

## 🎨 可视化工作流编辑器 (Workflow Editor Web)

> 🚀 **在线直接体验**：[https://xiaoyvyv.github.io/workflow-cmp/](https://xiaoyvyv.github.io/workflow-cmp/)
>
> 📖 **服务端与 AI 桥接文档**：详见 [docs/EditorServer.md](docs/EditorServer.md)。

`workflow-cmp` 提供了开箱即用的现代化 Web 可视化 DAG 编排编辑器与全平台设备桥接通信套件（`:workflow-editor-bridge` / `:workflow-editor-mcp`），支持纯浏览器前端免安装直接使用，亦可直接连接真机/桌面端无缝调试与执行。

### ✨ 编辑器主要特性

- **🖱️ 流畅拖拽与无限画布**：左侧节点库直接拖入画布，支持画布自由缩放、平移定位与框选。
- **📐 智能端口与连线校验**：严格校验控制流与数据流连接规则，防止循环死锁、类型不匹配或悬空端口。
- **🌲 自动层次排版 (Sugiyama Layout)**：一键对复杂 DAG 图进行整洁的分层自动排版，节点关系清晰直观。
- **🎯 精准错误高亮溯源**：工作流执行异常时自动弹出控制台，并在画布中精准单次高亮对应报错节点，点击空白即可恢复，排错一目了然。
- **⚡ 实时设备双向同步**：通过 WebSocket 与 Android / Desktop / iOS 宿主应用实时同步，支持下发运行、单步监控、副作用交互处理与取消操作。
- **🤖 MCP (Model Context Protocol) 赋能**：内置标准 MCP 协议服务，支持 Claude、Cursor 等 AI 助手直接理解节点 Spec、自动生成工作流并调用执行。
- **🔒 乐观锁版本并发保护**：多端编辑协同内置 Revision 修订号机制，保存时遇冲突自动提示是否覆盖或合并。

---

### 🌟 核心特性与应用场景

<table align="center" width="100%">
  <tr>
    <td width="50%" valign="top">
      <h4>⚡ 现代化 DAG 调度与 API 编排</h4>
      <ul>
        <li><b>拓扑并发调度</b>：严格按拓扑序计算依赖，支持死锁校验、多任务并发分支与屏障合流 (Fork & Join)</li>
        <li><b>协程全生命周期事件流</b>：基于 Kotlin Coroutines 与 Flow 驱动，节点状态、错误与副作用事件全量向外发射</li>
        <li><b>动态参数流转与容错</b>：上下文变量安全隔离，支持超时控制、失败自动重试与动态路径分支选择</li>
      </ul>
    </td>
    <td width="50%" valign="top">
      <h4>🕷️ 跨端数据抓取与格式清洗</h4>
      <ul>
        <li><b>一套逻辑，三端运行</b>：在 Android、iOS 与 Desktop (JVM) 保持执行逻辑完全一致，亦支持纯后台无头运行</li>
        <li><b>Ksoup DOM 抽取与解析</b>：无缝解析 HTML 源码，支持 CSS 选择器精准抽取、属性过滤与表格转 JSON 字典</li>
        <li><b>丰富数据清洗与加解密</b>：内置正则匹配替换、Base64 / Hex / URL 编解码、MD5 / SHA / AES 加密与 JSON / XML / CSV 互转</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h4>🤝 强解耦的副作用抽象与人机协同</h4>
      <ul>
        <li><b>人机协同 (Human-in-the-loop)</b>：工作流可在任意节点随时挂起，等待外部决策介入，用户操作完成后无缝恢复</li>
        <li><b>开箱即用默认交互套件</b>：内置通用交互节点（单选/多选列表弹窗、单行文本输入、二次确认对话框与生命周期进度条）</li>
        <li><b>多媒体预览与宿主隔离</b>：支持全屏大图画廊（双布局自由切换 + 手势缩放）与视频播放脚手架，节点不持有 UI 引用，可 100% 自定义替换</li>
      </ul>
    </td>
    <td width="50%" valign="top">
      <h4>🧩 强大表达式引擎与自定义节点扩展</h4>
      <ul>
        <li><b>内联语法动态求值</b>：支持三元运算符 <code>? :</code>、Elvis 兜底 <code>?:</code>、算术逻辑比较与成员方法调用 (<code>.trim</code> / <code>.length</code>)</li>
        <li><b>18 大类内置节点覆盖</b>：流程、控制、循环、JSON、对象、数组、文本、数学、日期时间、网络请求、沙箱文件与存储等</li>
        <li><b>自由组合与无限扩展</b>：基于标准的 <code>ActionNodeDefinition</code> 声明式插拔接口，快速扩展私有业务节点，自由编排任意复杂自动化工作流</li>
      </ul>
    </td>
  </tr>
</table>

---

## 目录

1. [可视化工作流编辑器 (Workflow Editor Web)](#-可视化工作流编辑器-workflow-editor-web)
2. [项目架构与模块划分](#1-项目架构与模块划分)
    1. [模块结构](#11-模块结构)
    2. [核心设计要点](#12-核心设计要点)
3. [执行模型与调度内核](#2-执行模型与调度内核)
    1. [调度机制与生命周期](#21-调度机制与生命周期)
    2. [分支与合流 (Fork & Join)](#22-分支与合流-fork--join)
    3. [平台副作用与挂起交互 (SideEffect)](#23-平台副作用与挂起交互-sideeffect)
4. [模板与表达式引擎](#3-模板与表达式引擎)
    1. [命名空间访问](#31-命名空间访问)
    2. [运算符优先级与语法规则](#32-运算符优先级与语法规则)
    3. [类型转换与真值判定](#33-类型转换与真值判定)
5. [内置节点参考](#4-内置节点参考)
    1. [流程控制与分支 (`flow.*`)](#41-流程控制与分支-flow)
    2. [逻辑判断 (`control.*`)](#42-逻辑判断-control)
    3. [循环控制与迭代 (`loop.*`)](#43-循环控制与迭代-loop)
    4. [上下文与变量操作 (`data.*`)](#44-上下文与变量操作-data)
    5. [JSON 对象操作 (`object.*`)](#45-json-对象操作-object)
    6. [数组集合操作 (`array.*`)](#46-数组集合操作-array)
    7. [文本与正则处理 (`text.*`)](#47-文本与正则处理-text)
    8. [数值与数学计算 (`math.*`)](#48-数值与数学计算-math)
    9. [日期与时间 (`date.*`)](#49-日期与时间-date)
    10. [URL 解析与构建 (`url.*`)](#410-url-解析与构建-url)
    11. [结构化数据解析 (`json.*`, `xml.*`, `csv.*`)](#411-结构化数据解析-json-xml-csv)
    12. [编解码与摘要哈希 (`codec.*`, `crypto.*`)](#412-编解码与摘要哈希-codec-crypto)
    13. [HTML 解析与抽取 (`html.*`)](#413-html-解析与抽取-html)
    14. [网络与下载 (`http.*`)](#414-网络与下载-http)
    15. [本地持久化存储 (`storage.*`)](#415-本地持久化存储-storage)
    16. [文件系统沙箱 (`file.*`)](#416-文件系统沙箱-file)
    17. [客户端动作与 UI 交互 (`action.*`, `ui.*`, `system.*`)](#417-客户端动作与-ui-交互-action-ui-system)
    18. [业务扩展节点 (`bilibili.*`)](#418-业务扩展节点-bilibili)
6. [错误处理与调试](#5-错误处理与调试)
7. [快速接入与示例](#6-快速接入与示例)
    1. [创建运行时 (`WorkflowRuntimeFactory`)](#61-创建运行时-workflowruntimefactory)
    2. [执行工作流与事件监听](#62-执行工作流与事件监听)
    3. [Compose UI 交互宿主挂载](#63-compose-ui-交互宿主挂载)
    4. [自定义节点扩展](#64-自定义节点扩展)
    5. [构建与测试命令](#65-构建与测试命令)

---

## 1. 项目架构与模块划分

### 1.1 模块结构

```text
workflow-cmp/
  ├── :workflow-core                 # [核心调度] 数据模型、DAG拓扑校验、并发调度引擎、表达式求值器
  │
  ├── 节点体系 (Node Modules)
  │   ├── :workflow-node-base        # [节点基础] 规格契约、元数据模型、节点注册中心
  │   ├── :workflow-node-control     # [流程/分支] 流程控制 (flow.*) 与逻辑判断 (control.*)
  │   ├── :workflow-node-data        # [数据/变换] 上下文变量、Object/Array/Text/Math/Date/URL/JSON/XML/CSV
  │   ├── :workflow-node-codec       # [编解码] Base64、Hex、URL 编解码、HTML 转义
  │   ├── :workflow-node-html        # [DOM抽取] 基于 Ksoup 的 HTML 解析与 CSS 选择器抽取
  │   ├── :workflow-node-crypto      # [密码摘要] MD5、SHA、HMAC、AES 加解密
  │   ├── :workflow-node-io          # [输入输出] HTTP 网络请求/下载、Preferences 本地存储、沙箱文件系统
  │   ├── :workflow-node-bilibili    # [特定业务扩展] Bilibili Wbi 签名与 API 适配节点
  │   ├── :workflow-node-all         # [聚合装配] 内置节点聚合与运行时工厂 (WorkflowRuntimeFactory)
  │   └── :workflow-node-testkit     # [测试套件] 节点规格与执行自动化测试工具
  │
  ├── 可视化编排与 AI 桥接 (Editor & MCP Modules)
  │   ├── :workflow-editor-bridge    # [全平台Bridge服务] 嵌入式 Ktor CIO 服务器、REST/WebSocket 同步与运行控制器
  │   ├── :workflow-editor-mcp       # [MCP AI工具库] 标准 Model Context Protocol 协议支持，赋能 Cursor/Claude 等 Agent
  │   └── workflow-editor-web/       # [Web可视化编辑器] 纯前端可视化 DAG 画布（支持拖拽、排版、单次高亮报错、GitHub Pages部署）
  │
  ├── 平台底座能力 (Platform Modules)
  │   ├── :workflow-platform         # [基础平台层] 音量/屏幕亮度管理、全屏控制器
  │   ├── :workflow-platform-room    # [Room存储] 基于 Room 3 的跨平台 CookieJar / 本地持久化支持
  │   └── :workflow-platform-ui      # [Compose平台层] 跨平台系统服务 (SystemService)、BackHandler
  │
  ├── UI交互与表现层 (UI Modules)
  │   ├── :workflow-ui-core          # [UI核心] SideEffect宿主状态控制器 (WorkflowSideEffectHostState)、标准弹窗
  │   ├── :workflow-ui-image         # [大图画廊] 跨平台全屏图片预览 (WorkflowImagePreviewPage，支持缩放/条漫)
  │   ├── :workflow-ui-video         # [视频播放] 跨平台内置视频播放器 (WorkflowVideoPreviewPage)
  │   ├── :workflow-ui-webview       # [网页/Cookie] 内置网页容器 (WorkflowWebScreen)、Cookie同步弹窗
  │   └── :workflow-ui-all           # [UI聚合] 开箱即用的 SideEffect 默认宿主 (WorkflowDefaultSideEffectHost)
  │
  ├── 应用与演示层 (App & Demo)
  │   ├── :shared                    # 跨平台演示模块 (WorkflowsViewModel)
  │   ├── :androidApp                # Android 入口工程
  │   └── :desktopApp                # Desktop (JVM) 入口工程
```

### 1.2 核心设计要点

1. **调度内核纯粹 (`:workflow-core`)**：纯 Kotlin 实现，负责 DAG 拓扑解析与并发调度，平台相关的网络、存储与交互通过通用接口解耦。
2. **轻量运行时组装**：通过 `createWorkflowRuntime(config)` 显式组装引擎与节点，便于嵌入到现有工程与架构中。
3. **副作用交互解耦**：节点不直接持有 UI 引用，涉及弹窗、输入或媒体展示时通过 `ActionSideEffect` 由宿主挂起处理并回传结果。
4. **按需依赖引入**：支持按需组合模块，基础逻辑仅需依赖 `:workflow-core` 与相关数据节点；如需客户端交互弹窗与预览能力，按需引入 `:workflow-ui-*` 模块。

---

## 2. 执行模型与调度内核

### 2.1 调度机制与生命周期

引擎调度基于就绪队列驱动的状态机模型。整体生命周期如下：

```text
       ┌────────────────────────┐
       │   入参校验与静态图检查   │ (ActionWorkflowValidator)
       └───────────┬────────────┘
                   ▼
       ┌────────────────────────┐
       │     初始化执行上下文     │ (ActionExecutionContext)
       └───────────┬────────────┘
                   ▼
  ┌───────────▶ 就绪队列提取节点 ◀───────────┐
  │            └───────────┬────────────┘           │
  │                        ▼                        │
  │            ┌────────────────────────┐           │
  │            │  配置插值与表达式求值   │ (ActionTemplateResolver)
  │            └───────────┬────────────┘           │
  │                        ▼                        │
  │            ┌────────────────────────┐           │
  │            │       执行节点行为      │           │
  │            └───────────┬────────────┘           │
  │                        ▼                        │
  │            ┌────────────────────────┐           │
  │            │ 更新变量表与步骤历史记录 │           │
  │            └───────────┬────────────┘           │
  │                        ▼                        │
  │            ┌────────────────────────┐           │
  │            │ 下游控制边计算与入队   │───────────┘
  │            └───────────┬────────────┘
  │                        ▼
  │               队列为空或遇终止节点
  │                        ▼
  │            ┌────────────────────────┐
  └────────────│ 构造终态日志并派发事件 │
               └────────────────────────┘
```

1. **响应式事件流**：`engine.execute(...)` 返回 `Flow<ActionExecutionEvent>`，外部调用方通过订阅该流获取 `Started`、`NodeStarted`、`NodeCompleted`、`SideEffectRequested`、`Completed` 与
   `Failed` 等生命周期事件。
2. **就绪状态判定**：仅当节点的所有必要前置依赖分支均已满足（`pendingPredecessors == 0`）时，节点才进入就绪队列。

### 2.2 分支与合流 (Fork & Join)

- **分支 (Fan-Out)**：节点的单一输出端口支持连接多条出边。上游端口触发后，所有关联的出边目标节点按就绪状态并发调度执行。
- **合流 (Fan-In)**：节点的输入端口支持连接多条入边。当节点存在多个激活前置路径时，调度器等待所有激活的前置分支执行完毕后仅触发该节点一次；未被激活的条件分支自动跳过，不阻塞合流。

### 2.3 平台副作用与挂起交互 (SideEffect)

工作流遇到需与用户交互或调用宿主设备能力（二次确认框、单行输入、单选列表、Cookie 同步、图片画廊等）时：

1. 节点构造对应的 `ActionSideEffect`，引擎发出 `ActionExecutionEvent.SideEffectRequested`，并交由 `sideEffectHandler` 处理。
2. 宿主端（如 Compose UI）调用挂起函数 `WorkflowSideEffectHostState.dispatch(effect)`，当前工作流协程自动挂起。
3. 用户完成界面交互（点击确定/取消、提交输入、关闭 BottomSheet）后，`dispatch` 唤醒并返回 `ActionSideEffectResult`，工作流获取结果后继续向后流转。

---

## 3. 模板与表达式引擎

节点配置项支持使用 `${...}` 占位符引用动态变量或内联表达式，由 `ActionTemplateResolver` 负责语法解析与高效求值。

### 3.1 命名空间访问

表达式支持通过点号（`.`）与下标（`[...]`）访问以下 6 个命名空间：

| 命名空间              | 作用说明                             | 访问示例                                   |
|:------------------|:---------------------------------|:---------------------------------------|
| **`input`**       | 外部触发工作流时传入的只读业务输入数据              | `${input.subjectId}`                   |
| **`environment`** | 宿主平台只读环境上下文（语言、平台版本等）            | `${environment.platform}`              |
| **`trigger`**     | 触发源元数据（触发类型、来源组件等）               | `${trigger.source}`                    |
| **`vars`**        | 工作流全局运行时变量字典                     | `${vars.pageIndex}`、`${vars.items[0]}` |
| **`steps`**       | 已执行节点的输出结果集合（以节点 ID 为索引）         | `${steps.http_get.body.data}`          |
| **`loop`**        | 当前所在最内层循环帧数据（含 `item` 与 `index`） | `${loop.item.id}`、`${loop.index}`      |

### 3.2 运算符优先级与语法规则

表达式解析器按以下优先级顺序（从低到高）求值：

| 优先级    | 运算类型         | 运算符 / 语法                     | 示例                                                |
|:-------|:-------------|:-----------------------------|:--------------------------------------------------|
| **1**  | 条件选择         | `? :` (三元运算符)                | `${vars.score >= 60 ? "及格" : "未及格"}`              |
| **2**  | 空值兜底         | `?:` (Elvis 运算符)             | `${vars.name ?: "默认值"}`                           |
| **3**  | 逻辑或          | `\|\|`                       | `${vars.a \|\| vars.b}`                           |
| **4**  | 逻辑与          | `&&`                         | `${vars.isReady && vars.hasMore}`                 |
| **5**  | 等值判断         | `==`, `!=`                   | `${vars.status == 200}`                           |
| **6**  | 比较运算         | `>`, `>=`, `<`, `<=`         | `${vars.count > 0}`                               |
| **7**  | 加法 / 减法      | `+`, `-` (二元加减与字符串拼接)        | `${vars.offset + 10}`、`"Page: " + vars.page`      |
| **8**  | 乘法 / 除法 / 取模 | `*`, `/`, `%`                | `${vars.width * vars.height}`、`${loop.index % 2}` |
| **9**  | 一元运算         | `!`, `-` (逻辑非 / 取负)          | `${!vars.enabled}`、`-${vars.delta}`               |
| **10** | 成员访问         | `.length`, `.trim` 等通用属性     | `${vars.title.length}`                            |
| **11** | 属性与下标访问      | `.property`, `[index]`       | `${steps.req.body.list[0]}`                       |
| **12** | 括号与字面量       | `(...)`, 字符串, 数字, 布尔, `null` | `${(vars.a + vars.b) * 2}`、`'text'`、`123`         |

### 3.3 类型转换与真值判定

1. **数值强转**：字符串数值参与数学运算或数值比较时自动转换为浮点数处理；整数运算结果自动规整为整型。
2. **字符串拼接**：二元 `+` 运算中若任一操作数为字符串，另一操作数自动转为字符串后连接。
3. **真值判定 (Truthiness)**：在逻辑运算符与条件节点中，以下值判定为 `false`，其余值均判定为 `true`：
    - `null` / `JsonNull`
    - 布尔值 `false`
    - 数字 `0` 或 `0.0`
    - 空字符串 `""`
    - 空数组 `[]`
    - 空对象 `{}`

---

## 4. 内置节点参考

### 4.1 流程控制与分支 (`flow.*`)

| 节点类型                  | 功能说明                  | 主要配置键                                          | 输出端口                       |
|:----------------------|:----------------------|:-----------------------------------------------|:---------------------------|
| **`flow.start`**      | 工作流执行起点               | 无                                              | `next`                     |
| **`flow.end`**        | 工作流正常终止点              | 无                                              | 无                          |
| **`flow.delay`**      | 阻塞挂起指定时长              | `delayMillis` (Long)                           | `next`                     |
| **`flow.stop`**       | 中断并退出工作流              | `message` (String, 可选)                         | 无                          |
| **`flow.assert`**     | 条件断言，表达式为 false 时抛出异常 | `condition` (Boolean/String)                   | `next`                     |
| **`flow.switch`**     | 多分支条件路由               | `cases` (Map<String, String>)                  | 匹配的 branch key 或 `default` |
| **`flow.log`**        | 打印调试日志信息              | `message`, `level`                             | `next`                     |
| **`flow.debug`**      | 调试断点信息输出              | `message`                                      | `next`                     |
| **`flow.try`**        | 异常捕获保护作用域起点           | 无                                              | `try`                      |
| **`flow.catch`**      | 异常处理分支入口              | 无                                              | `catch`                    |
| **`flow.finally`**    | 最终执行收尾分支入口            | 无                                              | `finally`                  |
| **`flow.call`**       | 调用子工作流                | `workflowId`, `outputKey`                      | `next`                     |
| **`flow.return`**     | 子工作流返回数据              | `output`                                       | 无                          |
| **`flow.parallel`**   | 并行分支起点                | 无                                              | `branches`                 |
| **`flow.join`**       | 并行分支汇合点               | `values`                                       | `next`                     |
| **`flow.retry`**      | 失败重试执行器               | `retryCount`, `retryDelayMillis`               | `next`                     |
| **`flow.timeout`**    | 超时控制作用域               | `timeoutMillis`                                | `next`                     |
| **`flow.wait_until`** | 循环轮询直至条件成立            | `condition`, `intervalMillis`, `maxWaitMillis` | `next`                     |
| **`flow.rate_limit`** | 限流等待控制                | `delayMillis`                                  | `next`                     |

### 4.2 逻辑判断 (`control.*`)

| 节点类型                                 | 功能说明                     | 必需配置键                        | 输出数据                                        |
|:-------------------------------------|:-------------------------|:-----------------------------|:--------------------------------------------|
| **`control.if`**                     | 条件分支路由                   | `condition`                  | 沿 `matched` (true) 或 `default` (false) 端口输出 |
| **`control.equals`**                 | 相等比较 (`left == right`)   | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.not_equals`**             | 不等比较 (`left != right`)   | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.greater_than`**           | 大于比较 (`left > right`)    | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.greater_than_or_equals`** | 大于等于比较 (`left >= right`) | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.less_than`**              | 小于比较 (`left < right`)    | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.less_than_or_equals`**    | 小于等于比较 (`left <= right`) | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.and`**                    | 逻辑与 (`left && right`)    | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.or`**                     | 逻辑或 (`left \|\| right`)  | `left`, `right`, `outputKey` | 布尔值                                         |
| **`control.not`**                    | 逻辑非 (`!value`)           | `value`, `outputKey`         | 布尔值                                         |
| **`control.is_null`**                | 空值判断 (`value == null`)   | `value`, `outputKey`         | 布尔值                                         |
| **`control.is_empty`**               | 空集合或空字符串判断               | `value`, `outputKey`         | 布尔值                                         |

### 4.3 循环控制与迭代 (`loop.*`)

| 节点类型                | 功能说明               | 必需配置键                         | 输出端口 / 目标                      |
|:--------------------|:-------------------|:------------------------------|:-------------------------------|
| **`loop.repeat`**   | 固定次数循环             | `count`, `maxIterations?`     | `body`, `completed`, `failure` |
| **`loop.for_each`** | 数组/集合遍历循环          | `items`, `maxIterations?`     | `body`, `completed`, `failure` |
| **`loop.while`**    | 条件循环（在条件满足时持续循环）   | `condition`, `maxIterations?` | `body`, `completed`, `failure` |
| **`loop.next`**     | 步进至下一轮迭代           | `loopId`                      | 无（由内部循环控制端口进入）                 |
| **`loop.continue`** | 跳过本轮循环剩余节点，进入下一轮迭代 | `loopId`                      | 无（由内部循环控制端口进入）                 |
| **`loop.break`**    | 中断并彻底退出当前循环        | `loopId`                      | 无（由内部循环控制端口进入）                 |

### 4.4 上下文与基础数据操作 (`data.*`)

| 节点类型                    | 功能说明        | 配置参数                                        | 写入目标               |
|:------------------------|:------------|:--------------------------------------------|:-------------------|
| **`data.set_variable`** | 设置单个全局变量    | `key`, `value`                              | `vars.<key>`       |
| **`data.template`**     | 文本模板插值渲染    | `template`, `outputKey`                     | `vars.<outputKey>` |
| **`data.coalesce`**     | 多个值中提取首个非空值 | `values`, `outputKey`                       | `vars.<outputKey>` |
| **`data.concat`**       | 字符串或数组连接拼接  | `values`, `separator?`, `outputKey`         | `vars.<outputKey>` |
| **`data.merge`**        | 多个对象合并      | `objects`, `mergeStrategy?`, `outputKey`    | `vars.<outputKey>` |
| **`data.assign`**       | 批量按路径赋值对象属性 | `object`, `assignments`, `outputKey`        | `vars.<outputKey>` |
| **`data.remove`**       | 按路径删除对象属性   | `object`, `path`, `outputKey`               | `vars.<outputKey>` |
| **`data.rename`**       | 按路径重命名对象属性  | `object`, `fromPath`, `toPath`, `outputKey` | `vars.<outputKey>` |
| **`data.pick`**         | 提取指定路径生成新对象 | `object`, `paths`, `outputKey`              | `vars.<outputKey>` |
| **`data.uuid`**         | 生成 UUID 字符串 | `outputKey`                                 | `vars.<outputKey>` |
| **`data.to_number`**    | 强制转换为数值类型   | `value`, `outputKey`                        | `vars.<outputKey>` |
| **`data.to_string`**    | 强制转换为字符串类型  | `value`, `outputKey`                        | `vars.<outputKey>` |
| **`data.to_boolean`**   | 强制转换为布尔类型   | `value`, `outputKey`                        | `vars.<outputKey>` |
| **`data.type_of`**      | 获取数据类型名称描述  | `value`, `outputKey`                        | `vars.<outputKey>` |

### 4.5 JSON 对象操作 (`object.*`)

| 节点类型                      | 功能说明                       | 必需配置键                                 | 输出数据    |
|:--------------------------|:---------------------------|:--------------------------------------|:--------|
| **`object.get`**          | 读取深层路径属性                   | `object`, `path`, `outputKey`         | 属性值     |
| **`object.set`**          | 设置对象键值对                    | `object`, `key`, `value`, `outputKey` | 新对象     |
| **`object.remove`**       | 删除对象指定键                    | `object`, `key`, `outputKey`          | 新对象     |
| **`object.omit`**         | 剔除指定键列表                    | `object`, `keys`, `outputKey`         | 新对象     |
| **`object.pick`**         | 仅保留指定键列表                   | `object`, `keys`, `outputKey`         | 新对象     |
| **`object.merge`**        | 合并多个对象                     | `objects`, `outputKey`                | 合并后的新对象 |
| **`object.keys`**         | 获取所有键名列表                   | `object`, `outputKey`                 | 字符串数组   |
| **`object.values`**       | 获取所有属性值列表                  | `object`, `outputKey`                 | 元素数组    |
| **`object.entries`**      | 转换为键值对数组 (`[[k, v], ...]`) | `object`, `outputKey`                 | 二维数组    |
| **`object.from_entries`** | 键值对数组还原为对象                 | `entries`, `outputKey`                | 对象      |
| **`object.has_key`**      | 判断是否包含指定键                  | `object`, `key`, `outputKey`          | 布尔值     |
| **`object.is_empty`**     | 判断是否为空对象 (`{}`)            | `object`, `outputKey`                 | 布尔值     |

### 4.6 数组集合操作 (`array.*`)

| 节点类型                        | 功能说明          | 必需配置键                                           | 输出数据         |
|:----------------------------|:--------------|:------------------------------------------------|:-------------|
| **`array.length`**          | 获取数组长度        | `values`, `outputKey`                           | 整数           |
| **`array.create`**          | 创建数组          | `values`, `outputKey`                           | 数组           |
| **`array.append`**          | 尾部添加元素        | `values`, `value`, `outputKey`                  | 新数组          |
| **`array.insert_at`**       | 指定位置插入元素      | `values`, `index`, `value`, `outputKey`         | 新数组          |
| **`array.remove_at`**       | 删除指定下标元素      | `values`, `index`, `outputKey`                  | 新数组          |
| **`array.filter`**          | 按条件过滤元素       | `values`, `operator`, `expected`, `outputKey`   | 过滤后的新数组      |
| **`array.map`**             | 提取元素指定字段生成新数组 | `values`, `fieldPath`, `outputKey`              | 新数组          |
| **`array.flat_map`**        | 提取字段并扁平化      | `values`, `fieldPath`, `outputKey`              | 一维新数组        |
| **`array.concat`**          | 拼接多个数组        | `values`, `outputKey`                           | 拼接后的新数组      |
| **`array.zip`**             | 双数组打包为元组数组    | `values`, `otherValues`, `outputKey`            | 二维元组数组       |
| **`array.take`**            | 获取前 N 个元素     | `values`, `count`, `outputKey`                  | 截取后的新数组      |
| **`array.drop`**            | 跳过前 N 个元素     | `values`, `count`, `outputKey`                  | 截取后的新数组      |
| **`array.contains`**        | 检查是否包含目标元素    | `values`, `value`, `outputKey`                  | 布尔值          |
| **`array.find`**            | 查找首个满足条件的元素   | `values`, `fieldPath`, `expected`, `outputKey`  | 元素或 `null`   |
| **`array.distinct`**        | 元素去重          | `values`, `outputKey`                           | 去重后的新数组      |
| **`array.sort`**            | 数组排序          | `values`, `outputKey`                           | 排序后的新数组      |
| **`array.reverse`**         | 反转数组          | `values`, `outputKey`                           | 反转后的新数组      |
| **`array.slice`**           | 切片截取区间元素      | `values`, `startIndex`, `endIndex`, `outputKey` | 截取后的新数组      |
| **`array.flatten`**         | 嵌套数组扁平化       | `values`, `outputKey`                           | 一维数组         |
| **`array.group_by`**        | 按指定字段分组       | `values`, `fieldPath`, `outputKey`              | 键值映射字典       |
| **`array.first`**           | 获取首个元素        | `values`, `outputKey`                           | 元素或 `null`   |
| **`array.last`**            | 获取末尾元素        | `values`, `outputKey`                           | 元素或 `null`   |
| **`array.sum`**             | 数值求和          | `values`, `outputKey`                           | 数值           |
| **`array.avg`**             | 数值求平均值        | `values`, `outputKey`                           | 数值           |
| **`array.min`** / **`max`** | 查找最小值 / 最大值   | `values`, `outputKey`                           | 数值           |
| **`array.chunk`**           | 数组按大小分块       | `values`, `size`, `outputKey`                   | 二维数组         |
| **`array.shuffle`**         | 随机打乱顺序        | `values`, `outputKey`                           | 新数组          |
| **`array.sample`**          | 随机采样单个元素      | `values`, `outputKey`                           | 元素或 `null`   |
| **`array.index_of`**        | 查找元素首个下标      | `values`, `value`, `outputKey`                  | 索引值（未找到为 -1） |
| **`array.intersection`**    | 两个数组求交集       | `values`, `otherValues`, `outputKey`            | 交集数组         |
| **`array.difference`**      | 两个数组求差集       | `values`, `otherValues`, `outputKey`            | 差集数组         |

### 4.7 文本与正则处理 (`text.*`)

| 节点类型                        | 功能说明        | 配置参数                                             | 输出数据        |
|:----------------------------|:------------|:-------------------------------------------------|:------------|
| **`text.length`**           | 计算字符长度      | `text`, `outputKey`                              | 整数          |
| **`text.trim`**             | 去除首尾空白字符    | `text`, `outputKey`                              | 字符串         |
| **`text.lowercase`**        | 转为小写字母      | `text`, `outputKey`                              | 字符串         |
| **`text.uppercase`**        | 转为大写字母      | `text`, `outputKey`                              | 字符串         |
| **`text.capitalize`**       | 首字母大写       | `text`, `outputKey`                              | 字符串         |
| **`text.repeat`**           | 重复拼接 N 次    | `text`, `count`, `outputKey`                     | 字符串         |
| **`text.reverse`**          | 反转字符顺序      | `text`, `outputKey`                              | 字符串         |
| **`text.index_of`**         | 查找子串首个下标    | `text`, `pattern`, `outputKey`                   | 整数（未找到为 -1） |
| **`text.template`**         | 变量插值渲染      | `template`, `object`, `outputKey`                | 渲染后的字符串     |
| **`text.split`**            | 按分隔符切分为数组   | `text`, `delimiter`, `outputKey`                 | 字符串数组       |
| **`text.regex_match`**      | 正则匹配与分组捕获   | `text`, `pattern`, `outputKey`                   | 匹配结果对象      |
| **`text.match_all`**        | 正则全局匹配列表    | `text`, `pattern`, `outputKey`                   | 匹配项数组       |
| **`text.substring`**        | 按照下标截取子串    | `text`, `startIndex`, `endIndex`, `outputKey`    | 字符串         |
| **`text.substring_before`** | 截取分隔符之前的文本  | `text`, `delimiter`, `outputKey`                 | 字符串         |
| **`text.substring_after`**  | 截取分隔符之后的文本  | `text`, `delimiter`, `outputKey`                 | 字符串         |
| **`text.replace`**          | 静态字符替换      | `text`, `pattern`, `replacement`, `outputKey`    | 字符串         |
| **`text.replace_regex`**    | 正则表达式替换     | `text`, `pattern`, `replacement`, `outputKey`    | 字符串         |
| **`text.join`**             | 使用指定连接符拼接数组 | `values`, `separator`, `outputKey`               | 字符串         |
| **`text.pad`**              | 文本填充对齐      | `text`, `padLength`, `padCharacter`, `outputKey` | 字符串         |
| **`text.format_number`**    | 数值格式化小数位    | `value`, `fractionDigits`, `outputKey`           | 字符串         |
| **`text.contains`**         | 检查是否包含子串    | `text`, `pattern`, `outputKey`                   | 布尔值         |
| **`text.starts_with`**      | 检查是否以前缀开头   | `text`, `pattern`, `outputKey`                   | 布尔值         |
| **`text.ends_with`**        | 检查是否以后缀结尾   | `text`, `pattern`, `outputKey`                   | 布尔值         |
| **`text.slugify`**          | 生成 URL 别名格式 | `text`, `outputKey`                              | 字符串         |
| **`text.truncate`**         | 超长截断与省略号补充  | `text`, `limit`, `outputKey`                     | 截断字符串       |

### 4.8 数值与数学计算 (`math.*`)

| 节点类型                       | 功能说明                     | 配置参数                               | 输出数据           |
|:---------------------------|:-------------------------|:-----------------------------------|:---------------|
| **`math.add`**             | 加法运算 (`left + right`)    | `left`, `right`, `outputKey`       | 数值             |
| **`math.subtract`**        | 减法运算 (`left - right`)    | `left`, `right`, `outputKey`       | 数值             |
| **`math.multiply`**        | 乘法运算 (`left * right`)    | `left`, `right`, `outputKey`       | 数值             |
| **`math.divide`**          | 除法运算 (`left / right`)    | `left`, `right`, `outputKey`       | 数值（除数为 0 抛出异常） |
| **`math.modulo`**          | 取模运算 (`left % right`)    | `left`, `right`, `outputKey`       | 数值（模数为 0 抛出异常） |
| **`math.min`** / **`max`** | 取两数中较小 / 较大值             | `left`, `right`, `outputKey`       | 数值             |
| **`math.pow`**             | 幂运算 ($left^{right}$)     | `left`, `right`, `outputKey`       | 数值             |
| **`math.sqrt`**            | 平方根运算 ($\sqrt{x}$)       | `value`, `outputKey`               | 数值（负数开方抛出异常）   |
| **`math.sum`**             | 列表元素累加求和                 | `values`, `outputKey`              | 数值             |
| **`math.avg`**             | 列表元素计算平均值                | `values`, `outputKey`              | 数值             |
| **`math.log`**             | 计算自然对数 $\ln(x)$          | `value`, `outputKey`               | 数值             |
| **`math.exp`**             | 计算自然指数 $e^x$             | `value`, `outputKey`               | 数值             |
| **`math.negate`**          | 数值取反 (`-value`)          | `value`, `outputKey`               | 数值             |
| **`math.round`**           | 四舍五入保留指定小数位              | `value`, `decimals`, `outputKey`   | 数值             |
| **`math.floor`**           | 向下取整                     | `value`, `outputKey`               | 整数             |
| **`math.ceil`**            | 向上取整                     | `value`, `outputKey`               | 整数             |
| **`math.abs`**             | 计算绝对值                    | `value`, `outputKey`               | 数值             |
| **`math.random`**          | 生成指定区间 `[min, max)` 伪随机数 | `min`, `max`, `outputKey`          | 数值             |
| **`math.clamp`**           | 数值区间截断约束                 | `value`, `min`, `max`, `outputKey` | 数值             |

### 4.9 日期与时间 (`date.*`)

| 节点类型                     | 功能说明                 | 必需配置键                                                  | 输出数据                 |
|:-------------------------|:---------------------|:-------------------------------------------------------|:---------------------|
| **`date.now`**           | 获取系统当前时间戳 (毫秒)       | `outputKey`                                            | Long 毫秒值             |
| **`date.format`**        | 时间戳格式化为 ISO 8601 字符串 | `timestamp`, `outputKey`                               | 格式化日期字符串             |
| **`date.parse`**         | 日期字符串解析为时间戳          | `text`, `outputKey`                                    | Long 毫秒值             |
| **`date.add`**           | 增加指定时间跨度             | `timestamp`, `count`, `unit`, `outputKey`              | Long 毫秒值             |
| **`date.subtract`**      | 减少指定时间跨度             | `timestamp`, `count`, `unit`, `outputKey`              | Long 毫秒值             |
| **`date.diff`**          | 计算两时间戳差值             | `timestampLeft`, `timestampRight`, `unit`, `outputKey` | 数值                   |
| **`date.relative_time`** | 转换为相对时间描述            | `timestamp`, `outputKey`                               | 相对时间字符串 (如 `"5分钟前"`) |
| **`date.get_component`** | 提取时间分量 (年月日时分秒)      | `timestamp`, `outputKey`                               | 时间分量字典               |

### 4.10 URL 解析与构建 (`url.*`)

| 节点类型                      | 功能说明                    | 必需配置键                                     | 输出数据           |
|:--------------------------|:------------------------|:------------------------------------------|:---------------|
| **`url.parse`**           | 解析 URL 结构 (协议、域名、路径、参数) | `url`, `outputKey`                        | URL 组成对象       |
| **`url.build`**           | 由基准路径与参数对象构造完整 URL      | `baseUrl`, `queryParameters`, `outputKey` | 完整 URL 字符串     |
| **`url.set_query_param`** | 设置或覆盖 URL 查询参数          | `url`, `key`, `value`, `outputKey`        | 新 URL 字符串      |
| **`url.get_query_param`** | 读取 URL 指定查询参数值          | `url`, `key`, `outputKey`                 | 参数值字符串或 `null` |

### 4.11 结构化数据解析 (`json.*`, `xml.*`, `csv.*`)

| 节点类型                 | 功能说明                  | 必需配置键                         | 输出数据                             |
|:---------------------|:----------------------|:------------------------------|:---------------------------------|
| **`json.parse`**     | JSON 文本反序列化为对象或数组     | `text`, `outputKey`           | JsonElement 结构                   |
| **`json.stringify`** | 对象序列化为 JSON 文本        | `value`, `outputKey`          | 字符串                              |
| **`json.extract`**   | 按 JSONPath 表达式提取节点    | `source`, `path`, `outputKey` | 匹配到的节点值                          |
| **`json.validate`**  | 校验文本是否为合法 JSON        | `text`, `outputKey`           | 布尔值                              |
| **`xml.parse`**      | XML/RSS 文本解析为 JSON 结构 | `text`, `outputKey`           | JsonObject 结构                    |
| **`xml.stringify`**  | JSON 结构序列化为 XML 文本    | `data`, `outputKey`           | XML 字符串                          |
| **`csv.parse`**      | CSV 表格解析为对象记录数组       | `text`, `outputKey`           | 字典列表 `List<Map<String, String>>` |
| **`csv.stringify`**  | 记录数组转换为 CSV 文本        | `items`, `outputKey`          | CSV 格式文本                         |

### 4.12 编解码与摘要哈希 (`codec.*`, `crypto.*`)

| 节点类型                                         | 功能说明                                | 必需配置键                                      | 输出数据          |
|:---------------------------------------------|:------------------------------------|:-------------------------------------------|:--------------|
| **`codec.base64_encode`** / **`decode`**     | 标准 Base64 编解码                       | `text`, `outputKey`                        | 字符串           |
| **`codec.base64_url_encode`** / **`decode`** | URL 安全的 Base64 编解码                  | `text`, `outputKey`                        | 字符串           |
| **`codec.hex_encode`** / **`decode`**        | 十六进制 (Hex) 编解码                      | `text`, `outputKey`                        | 字符串           |
| **`codec.url_encode`** / **`decode`**        | URL 百分号编解码                          | `text`, `outputKey`                        | 字符串           |
| **`codec.html_escape`** / **`unescape`**     | HTML 字符实体转义与反转义                     | `text`, `outputKey`                        | 字符串           |
| **`crypto.hash`**                            | 计算哈希摘要 (支持 SHA-256, SHA-512, MD5 等) | `text`, `algorithm`, `outputKey`           | 哈希十六进制字符串     |
| **`crypto.hmac`**                            | 计算 HMAC 签名                          | `text`, `secret`, `algorithm`, `outputKey` | 签名十六进制字符串     |
| **`crypto.encrypt`** / **`decrypt`**         | AES 对称加密与解密                         | `text`, `key`, `algorithm`, `outputKey`    | 密文 / 明文字符串    |
| **`crypto.random_bytes`**                    | 生成指定长度伪随机字节串                        | `length`, `outputKey`                      | 十六进制随机字符串     |
| **`crypto.uuid`**                            | 生成标准 UUID V4 标识                     | `outputKey`                                | 36 位 UUID 字符串 |

### 4.13 HTML 解析与抽取 (`html.*`)

HTML 解析节点基于 **Ksoup** 引擎构建，负责 DOM 树的解析、选择器检索、层次遍历与数据抽取。

- **输入类型**：支持标准 HTML 文档、HTML 片段字符串或 HTML 字符串数组。
- **输出格式**：
    - 单元素定位（如 `html.parse`、`html.select_first`、`html.parent` 等）：输出为 HTML 字符串（未匹配时为 `null`）。
    - 元素集合（如 `html.select`、`html.children` 等）：输出为 HTML 字符串数组 `List<String>`（未匹配时为空数组 `[]`）。
    - 属性与内容提取（如 `html.text`、`html.attr` 等）：输入单元素输出标量值；输入多元素输出标量值数组。

| 节点类型                     | 功能说明                              | 配置参数                                             | 输出数据                             |
|:-------------------------|:----------------------------------|:-------------------------------------------------|:---------------------------------|
| **`html.parse`**         | 解析 HTML 源码并输出规范化文档字符串             | `html` / `source` (String), `outputKey`          | Document 完整 HTML 字符串             |
| **`html.remove`**        | 根据 CSS 选择器剔除匹配标签                  | `source`, `selector` (String), `outputKey`       | 剔除后的 HTML 字符串                    |
| **`html.select`**        | 执行 CSS 选择器匹配，返回所有匹配项的 HTML 源码列表   | `source`, `selector`, `outputKey`                | 匹配元素的 HTML 字符串数组 `List<String>`  |
| **`html.select_first`**  | 执行 CSS 选择器匹配，返回首个匹配项的 HTML 源码     | `source`, `selector`, `outputKey`                | 首个匹配项 HTML 字符串；未匹配为 `null`       |
| **`html.parent`**        | 获取当前元素的直接父级元素 HTML                | `source`, `outputKey`                            | 父级 HTML 字符串；无父级为 `null`          |
| **`html.children`**      | 获取当前元素的全部直接子元素 HTML 列表            | `source`, `outputKey`                            | 子元素 HTML 字符串数组                   |
| **`html.first`**         | 获取元素集合中的首个元素                      | `source`, `outputKey`                            | 首个元素 HTML；空集合为 `null`            |
| **`html.last`**          | 获取元素集合中的末尾元素                      | `source`, `outputKey`                            | 末尾元素 HTML；空集合为 `null`            |
| **`html.get`**           | 按 0 起始索引获取集合中指定位置元素               | `source`, `index` (Int), `outputKey`             | 目标元素 HTML；越界为 `null`             |
| **`html.size`**          | 统计元素集合中包含的节点数量                    | `source`, `outputKey`                            | 元素数量 (Int)                       |
| **`html.attr`**          | 提取指定属性名称的值                        | `source`, `attribute` (String), `outputKey`      | 属性值字符串（多元素时为字符串数组）               |
| **`html.tag`**           | 提取元素的小写标签名称                       | `source`, `outputKey`                            | 标签名字符串（多元素时为字符串数组）               |
| **`html.text`**          | 提取节点及其子树的纯文本内容                    | `source`, `outputKey`                            | 纯文本字符串（多元素时为字符串数组）               |
| **`html.data`**          | 提取 `<script>` 或 `<style>` 标签的内部数据 | `source`, `outputKey`                            | 原始字符数据字符串                        |
| **`html.value`**         | 提取表单输入控件的值                        | `source`, `outputKey`                            | 控件取值字符串                          |
| **`html.id`**            | 提取元素的 `id` 属性                     | `source`, `outputKey`                            | ID 字符串（未声明为空字符串）                 |
| **`html.html`**          | 提取元素的内部 HTML (`innerHTML`)        | `source`, `outputKey`                            | 内部 HTML 字符串                      |
| **`html.outer_html`**    | 提取包含标签自身的完整 HTML (`outerHTML`)    | `source`, `outputKey`                            | 完整 HTML 字符串                      |
| **`html.has_class`**     | 校验元素是否包含指定 CSS 类名                 | `source`, `className` (String), `outputKey`      | 布尔值 `true` / `false`             |
| **`html.map`**           | 对集合内每个元素执行指定提取操作并收集为数组            | `source`, `operation`, `attribute?`, `outputKey` | 结果数组 `JsonArray<String>`         |
| **`html.table_to_json`** | 将 `<table>` 元素解析为结构化字典列表          | `html`, `selector?` (默认 `"table"`), `outputKey`  | 字典数组 `List<Map<String, String>>` |

### 4.14 网络与下载 (`http.*`)

| 节点类型                | 功能说明                                       | 配置参数                                                                                                                                                                                                                                  | 输出结构说明                                                                                                                                                                                                       |
|:--------------------|:-------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`http.request`**  | 发起 HTTP/HTTPS 请求（支持 GET/POST/PUT/DELETE 等） | `url` (必填), `method?` (默认 `"GET"`), `headers?`, `query?`, `body?`, `bodyType?` (`json`/`form`/`text`/`bytes`), `contentType?`, `timeoutMillis?`, `retryCount?`, `retryDelayMillis?`, `useLocalCookieStorage?` (Boolean), `outputKey?` | 输出包含：<br>• `statusCode` (Int 状态码)<br>• `isSuccess` (Boolean 是否成功)<br>• `contentType` (String 媒体类型)<br>• `rawBody` (String 原始文本响应体)<br>• `body` (JsonElement 结构化对象/数组/标量)                                     |
| **`http.download`** | 下载远程网络文件并流式写入工作流沙箱目录                       | `url` (必填), `path` (必填，沙箱相对保存目录), `fileName?` (可选指定文件名，默认根据 Content-Disposition 或 URL 自动推断), `outputKey` (必填), `headers?`, `query?`, `method?`, `timeoutMillis?`, `retryCount?`, `retryDelayMillis?`, `useLocalCookieStorage?`        | 输出包含：<br>• `statusCode` (Int 状态码)<br>• `isSuccess` (Boolean 是否成功)<br>• `contentType` (String 媒体类型)<br>• `fileName` (String 保存文件名)<br>• `filePath` (String 沙箱内相对文件路径)<br>• `rawBody` / `body` (String 沙箱文件路径) |

### 4.15 本地持久化存储 (`storage.*`)

| 节点类型                             | 功能说明                | 配置参数                        | 访问目标             |
|:---------------------------------|:--------------------|:----------------------------|:-----------------|
| **`storage.preferences_get`**    | 读取 Preferences 存储数据 | `key`, `outputKey`          | 读取本地 Preferences |
| **`storage.preferences_set`**    | 写入 Preferences 键值对  | `key`, `value`, `outputKey` | 写入本地 Preferences |
| **`storage.preferences_delete`** | 删除指定 Preferences 键  | `key`                       | 移除指定 Key         |
| **`storage.preferences_has`**    | 检查指定键是否存在           | `key`, `outputKey`          | 返回布尔值            |
| **`storage.preferences_clear`**  | 清空当前工作流的本地存储        | 无                           | 清空 Preferences   |

### 4.16 文件系统沙箱 (`file.*`)

所有文件系统节点均运行于沙箱隔离环境中，仅允许访问工作流独占的 `workflowId/` 目录。路径越界或包含 `..` 穿越行为将被拒绝并抛出 `file_access_denied` 错误。

| 节点类型                             | 功能说明                          | 必需配置键                                  |
|:---------------------------------|:------------------------------|:---------------------------------------|
| **`file.read_text`**             | 读取沙箱内 UTF-8 文本文件              | `path`, `outputKey`                    |
| **`file.write_text`**            | 写入 UTF-8 文本（支持 `append` 追加模式） | `path`, `text`, `append?`, `outputKey` |
| **`file.create`**                | 创建空文件（不覆盖已有文件）                | `path`, `outputKey`                    |
| **`file.get_working_directory`** | 获取当前工作流沙箱目录的绝对路径              | `outputKey`                            |
| **`file.delete`**                | 删除沙箱内的文件或空目录                  | `path`, `outputKey`                    |
| **`file.exists`**                | 检查指定路径的文件或目录是否存在              | `path`, `outputKey`                    |
| **`file.mkdir`**                 | 递归创建目录                        | `path`, `outputKey`                    |
| **`file.list`**                  | 列举指定目录下的全部子项名称                | `path`, `outputKey`                    |
| **`file.copy`**                  | 复制文件或目录                       | `fromPath`, `toPath`, `outputKey`      |
| **`file.move`**                  | 移动或重命名文件或目录                   | `fromPath`, `toPath`, `outputKey`      |
| **`file.compress_zip`**          | 将文件或目录列表压缩为 ZIP 包             | `paths`, `toPath`, `outputKey`         |
| **`file.extract_zip`**           | 安全解压 ZIP 包（带包大小与解压条目限制）       | `fromPath`, `toPath`, `outputKey`      |

### 4.17 客户端动作与 UI 交互 (`action.*`, `ui.*`, `system.*`, `image.*`, `video.*`)

#### 客户端系统动作节点 (`action.*`)

| 节点类型                           | 功能说明                            | 配置参数                 | 触发机制             | 所需能力                                     |
|:-------------------------------|:--------------------------------|:---------------------|:-----------------|:-----------------------------------------|
| **`action.open_internal_web`** | 在应用内置 WebView 容器中打开网页           | `url` (必填), `title?` | 派发 UI 路由副作用      | `NETWORK`                                |
| **`action.open_external_url`** | 唤起系统默认浏览器打开 URL                 | `url` (必填)           | 派发系统浏览器调用副作用     | 无                                        |
| **`action.open_external_app`** | 唤起第三方 App 协议或 URI Scheme        | `uri` (必填)           | 派发系统协议调用副作用      | 无                                        |
| **`action.sync_cookie`**       | 打开内置 WebView 交互登录并同步 Cookie 至本地 | `url` (必填), `title?` | 同步写入本地 CookieJar | `NETWORK`, `NETWORK_LOCAL_COOKIE_ACCESS` |
| **`action.show_toast`**        | 弹出系统 Toast 轻量提示                 | `message` (必填)       | 派发 UI Toast 副作用  | 无                                        |
| **`action.write_clipboard`**   | 写入文本到系统剪贴板                      | `text` (必填)          | 派发系统剪贴板写入副作用     | `CLIPBOARD_WRITE`                        |
| **`action.read_clipboard`**    | 读取系统剪贴板文本                       | `outputKey`          | 派发系统剪贴板读取副作用     | `CLIPBOARD_READ`                         |

#### 用户界面交互对话框节点 (`ui.*`)

| 节点类型                      | 功能说明               | 配置参数                                                                                      | 控制流与输出行为                             |
|:--------------------------|:-------------------|:------------------------------------------------------------------------------------------|:-------------------------------------|
| **`ui.confirm`**          | 弹出二次确认弹窗，挂起等待用户操作  | `message` (必填), `title?`                                                                  | 确认沿 `success` 端口继续；取消沿 `cancel` 端口继续 |
| **`ui.input_dialog`**     | 弹出单行文本输入框，挂起等待用户输入 | `title?`, `defaultValue?`, `outputKey`                                                    | `output.<outputKey>` 接收用户输入的字符串      |
| **`ui.select_dialog`**    | 弹出单选列表对话框供用户选择     | `options` (Array<Object>), `title?`, `outputKey`                                          | `output.<outputKey>` 接收选中的选项值        |
| **`ui.progress_dialog`**  | 显示全局进度弹窗           | `title?`, `message?`, `mode` (`determinate`/`indeterminate`), `progress?`, `maxProgress?` | 派发弹窗创建副作用，流程非阻塞推进                    |
| **`ui.progress_update`**  | 更新现有进度弹窗显示内容与数值    | `message?`, `progress?`                                                                   | 派发更新副作用，流程非阻塞推进                      |
| **`ui.progress_dismiss`** | 关闭并销毁当前全局进度弹窗      | 无                                                                                         | 派发销毁副作用，流程非阻塞推进                      |

#### 硬件与多媒体节点 (`system.*`, `ui.*`)

| 节点类型                      | 功能说明          | 配置参数                                      | 触发机制      |
|:--------------------------|:--------------|:------------------------------------------|:----------|
| **`system.share`**        | 唤起系统原生分享面板    | `text` (必填)                               | 派发系统分享副作用 |
| **`system.notification`** | 发送系统通知栏消息     | `content` (必填), `title?`                  | 派发系统通知副作用 |
| **`system.vibrate`**      | 触发设备触觉震动      | 无                                         | 派发硬件震动副作用 |
| **`ui.image_preview`**    | 唤起应用全屏大图预览画廊  | `images` (Array<String>), `index?` (默认 0) | 派发画廊查看副作用 |
| **`ui.video_preview`**    | 唤起应用内置播放器全屏播放 | `url` (必填), `headers?`                    | 派发视频播放副作用 |

### 4.18 业务扩展节点 (`bilibili.*`)

| 节点类型                    | 功能说明                                         | 必需配置键                                    | 输出数据             |
|:------------------------|:---------------------------------------------|:-----------------------------------------|:-----------------|
| **`bilibili.sign_url`** | 针对 Bilibili Web 搜索接口执行 WBI 签名加密，并自动补齐客户端公共参数 | `url`, `outputKey`, `imgKey?`, `subKey?` | 签名后的完整请求 URL 字符串 |

---

## 5. 错误处理与调试

工作流在静态校验未通过或运行期发生异常时，会抛出结构化的 `ActionWorkflowException`，携带具体的错误标识与排查建议：

- **静态校验失败 (`ActionValidationException`)**：如拓扑成环、节点 ID 冲突、缺少必需输入参数等。
- **节点执行失败 (`ActionNodeExecutionException`)**：如网络超时、数值计算除零、表达式解析失败等。

异常与失败事件均包含完整的上下文诊断字段（`code`、`nodeId`、`hint` 与输入配置快照）：

```json
{
   "code": "node_execution_failed",
   "message": "节点 [divide_node] 执行失败",
   "nodeId": "divide_node",
   "nodeType": "math.divide",
   "hint": "除数不能为 0，请在计算前通过 control.if 校验。"
}
```

### 日志监听

在开发与排查时，可通过 `ActionWorkflowTraceLogger` 挂载外部日志监听器：

```kotlin
ActionWorkflowTraceLogger.addListener { tag, priority, message ->
   println("[$tag] $message")
}
```

---

## 6. 快速接入与示例

### 6.1 创建运行时 (`WorkflowRuntimeFactory`)

通过工厂函数直接创建工作流运行时环境（包含调度引擎、节点注册表与校验器）：

```kotlin
import com.xiaoyv.workflow.di.WorkflowRuntimeConfig
import com.xiaoyv.workflow.di.createWorkflowRuntime
import com.xiaoyv.workflow.port.impl.DefaultActionHttpRequestExecutor
import io.ktor.client.HttpClient

// 1. 初始化宿主网络 Client
val httpClient = HttpClient()

// 2. 组装 WorkflowRuntime（包含引擎、节点注册表、校验器与序列化 Codec）
val runtime = createWorkflowRuntime(
    config = WorkflowRuntimeConfig(
        httpRequestExecutor = DefaultActionHttpRequestExecutor(httpClient),
        // 可选提供：preferencesStore, fileStorage, logger 等自定义实现
    )
)

val engine = runtime.engine
val registry = runtime.registry
```

### 6.2 执行调用与事件流消费

外部业务层或 ViewModel 触发工作流执行，订阅生命周期 Flow：

```kotlin
import com.xiaoyv.workflow.model.execution.ActionExecutionContext
import com.xiaoyv.workflow.model.execution.ActionExecutionEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

val executionJob = scope.launch {
    engine.execute(
        workflow = sampleWorkflow,
        initialContext = ActionExecutionContext(
            input = buildJsonObject {
                put("nameCn", JsonPrimitive("孤独摇滚"))
                put("id", JsonPrimitive(328609))
            },
            environment = buildJsonObject { put("locale", JsonPrimitive("zh-CN")) },
            trigger = buildJsonObject { put("source", JsonPrimitive("detail_page")) },
        ),
        sideEffectHandler = { effect ->
            // 由宿主 UI 控制器挂起分发（见下节）
            sideEffectHostState.dispatch(effect)
        },
    ).collect { event ->
        when (event) {
            is ActionExecutionEvent.Started -> println("工作流启动: ${event.workflowId}")
            is ActionExecutionEvent.NodeStarted -> println("节点开始: ${event.nodeId}")
            is ActionExecutionEvent.NodeCompleted -> println("节点完成: ${event.nodeId}, 出口: ${event.outputPortId}")
            is ActionExecutionEvent.SideEffectRequested -> println("触发系统动作: ${event.effect}")
            is ActionExecutionEvent.Completed -> println("执行成功，耗时: ${event.log.finishedAt - event.log.startedAt}ms")
            is ActionExecutionEvent.Failed -> println("执行异常: ${event.error.message}")
        }
    }
}
```

### 6.3 Compose UI 交互宿主挂载

在 Compose Multiplatform 界面中，配合 `:workflow-ui-core` 与 `:workflow-ui-all` 即可一站式挂载各类副作用弹窗与多媒体页面：

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xiaoyv.workflow.ui.all.WorkflowDefaultSideEffectHost
import com.xiaoyv.workflow.ui.core.rememberWorkflowSideEffectHostState

@Composable
fun AppContent() {
    // 1. 创建副作用宿主状态机（内置二次确认、输入、单选、进度弹窗及页面路由队列）
    val effectHostState = rememberWorkflowSideEffectHostState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 主业务界面（点击按钮运行工作流，将 effectHostState.dispatch 传入 sideEffectHandler）
        MainScreen(
            onRunWorkflow = { workflow ->
                // 调用 engine.execute(..., sideEffectHandler = { effectHostState.dispatch(it) })
            }
        )

        // 2. 放置聚合交互宿主，自动挂载对话框、全屏图片预览、视频播放器与内置浏览器
        WorkflowDefaultSideEffectHost(
            hostState = effectHostState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
```

### 6.4 自定义节点扩展

通过实现 `ActionNodeDefinition` 与 `ActionNodeExecutor`，可快速声明与扩展业务私有节点：

1. **定义规格 (`ActionNodeSpec`)**：声明节点类型、输入/输出端口与必需配置项。
2. **实现执行器 (`ActionNodeExecutor`)**：从 `ActionExecutionContext` 读取入参并执行逻辑，返回 `ActionNodeExecutionResult`。
3. **注册节点**：在调用 `createWorkflowRuntime(config, nodeDefinitions = listOf(customNode))` 时追加注册即可。

### 6.5 构建与测试命令

```bash
# 1. 编译 shared 模块公共代码元数据
./gradlew :shared:compileCommonMainKotlinMetadata

# 2. 编译 Android 端 shared 产物
./gradlew :shared:compileAndroidMain

# 3. 运行桌面端 Compose 示例应用
./gradlew :desktopApp:run

# 4. 执行全工程单元测试
./gradlew testDebugUnitTest
```
