# Workflow CMP 迁移设计与执行记录

## 目标

将 Bangumi `shared:data-workflow` 迁移为可独立复用的 Kotlin Multiplatform 工作流组件。迁移后的核心不依赖 Compose、Bangumi 业务模型或具体平台实现；内置节点按能力和外部依赖拆分，并由聚合模块提供完整节点集。

## 模块边界

| 模块                        | 职责                                                                  | 允许依赖                                                          |
|---------------------------|---------------------------------------------------------------------|---------------------------------------------------------------|
| `workflow-core`           | 图定义、编码、模板解析、节点协议/注册中心、引擎、异常与端口接口                                    | Kotlinx；不依赖节点、Compose、平台实现                                    |
| `workflow-platform`       | 文件、偏好、HTTP、日志等默认基础设施实现                                              | `workflow-core` 与平台无关的基础设施库                                   |
| `workflow-node-base`      | 内置节点共用 DSL 与结果辅助函数                                                  | `workflow-core`                                               |
| `workflow-node-control`   | flow/control/loop 节点                                                | `workflow-node-base`、`workflow-node-data`、`workflow-platform` |
| `workflow-node-data`      | data/math/text/object/array 节点                                      | `workflow-node-base`                                          |
| `workflow-node-codec`     | codec、URL、CSV、日期、JSON 节点                                            | `workflow-node-base`                                          |
| `workflow-node-html`      | HTML/XML 节点；唯一引入 Ksoup                                              | `workflow-node-base`、Ksoup                                    |
| `workflow-node-crypto`    | 摘要哈希节点                                                              | `workflow-node-base`                                          |
| `workflow-node-io`        | HTTP、文件、存储和副作用节点                                                    | `workflow-node-base`、`workflow-platform`                      |
| `workflow-node-bilibili`  | Bilibili URL 扩展节点                                                   | `workflow-node-base`、CryptoHash                               |
| `workflow-node-all`       | 聚合所有节点并提供 `builtInActionNodeDefinitions`                            | 全部节点模块                                                        |
| `workflow-node-testkit`   | 节点测试的共享 fixture，不含产品节点                                              | 全部节点模块                                                        |
| `workflow-ui`             | 跨平台副作用状态和基础 UI 协议                                                   | `workflow-core`、`workflow-node-io`                            |
| `workflow-ui-side-effect` | 默认副作用宿主                                                             | `workflow-ui`                                                 |
| `workflow-ui-image`       | 对应 `PreviewMain` 的多图分页、缩放与加载状态页面                                    | `workflow-ui`、Coil、ZoomImage                                  |
| `workflow-ui-video`       | 对应 `PreviewVideo` 的完整播放 UI：手势、控制栏、进度、倍速、全屏与状态浮层                     | `workflow-ui`、`workflow-platform`、MediaMP                     |
| `workflow-ui-webview`     | 对应 `WebScreen` 的跨平台 WebView 与加载进度页面                                 | `workflow-ui`、Compose WebView                                 |
| `workflow-ui-all`         | 聚合全部工作流 UI 与默认图片、视频、网页页面入口                                          | 全部 `workflow-ui-*` 模块                                         |
| `shared`                  | 可运行的工作流交互示例：原 `features/workflows` 的 Screen、ViewModel、样例、节点图卡片与状态模型 | 工作流模块与 Compose UI                                             |

## API 与兼容策略

公共 Kotlin 包名统一由 `com.xiaoyv.bangumi.shared.data.workflow` 改为 `com.xiaoyv.workflow`，避免新库携带宿主业务命名。原始测试按对应模块迁移，保留断言与测试场景。`ActionNodeRegistry`
改为显式接收节点定义；完整内置集由 `workflow-node-all` 创建，消除 `core -> node-all -> core` 循环依赖。

原 Bangumi UI 所依赖的 Cookie 存储、动作处理器和资源文本改为 UI 层的可配置宿主回调与通用默认文本；因此库不再依赖任何 Bangumi 业务模块。

原 `features/workflows` 交互测试页面整体迁入 `shared/src/commonMain/kotlin/com/xiaoyv/workflow/demo`。工作流样例、`WorkflowsViewModel`、执行日志、节点图与副作用分发逻辑保持原有实现；Bangumi
专属导航、资源、顶栏和页面状态容器以同模块的轻量通用宿主替代，避免反向引入 Bangumi 依赖。

## 验收标准

1. `workflow-core` 没有 Compose、Bangumi 或内置节点实现依赖。
2. Ksoup 仅出现在 `workflow-node-html`。
3. 所有原始测试已迁入，且在新项目完整测试任务中通过。
4. Gradle 设置包含所有新增节点、预览 UI 与聚合模块。
5. `workflow-node-all` 不含测试；测试只归属其节点模块或共享 `workflow-node-testkit`。

## 执行状态

已完成。

验证命令：

```shell
./gradlew test :workflow-ui-all:compileKotlinJvm
```

结果：`BUILD SUCCESSFUL`。聚合模块 `workflow-node-all` 不再包含任何测试代码；节点测试已按 control、data、html、codec、io、platform 与 core 的职责迁入对应模块，公共 fixture 位于
`workflow-node-testkit`。媒体预览所需的 ZoomImage、MediaMP 与 Compose WebView 已作为对应细分 UI 模块的直接依赖迁入。完整视频控件来自原 `shared/ui-video`；亮度、音量和全屏控制器归入
`workflow-platform`。
