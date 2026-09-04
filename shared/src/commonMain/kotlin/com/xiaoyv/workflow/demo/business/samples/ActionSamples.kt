package com.xiaoyv.workflow.demo.business.samples

import com.xiaoyv.workflow.model.definition.ActionWorkflow
import com.xiaoyv.workflow.model.spec.ActionCapability
import com.xiaoyv.workflow.model.spec.ActionClipboardConfigKey
import com.xiaoyv.workflow.model.spec.ActionConfirmConfigKey
import com.xiaoyv.workflow.model.spec.ActionControlPortId
import com.xiaoyv.workflow.model.spec.ActionDataConfigKey
import com.xiaoyv.workflow.model.spec.ActionFlowConfigKey
import com.xiaoyv.workflow.model.spec.ActionImagePreviewConfigKey
import com.xiaoyv.workflow.model.spec.ActionInputDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionLoopConfigKey
import com.xiaoyv.workflow.model.spec.ActionNodeType
import com.xiaoyv.workflow.model.spec.ActionNotificationConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenAppConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenUrlConfigKey
import com.xiaoyv.workflow.model.spec.ActionOpenWebConfigKey
import com.xiaoyv.workflow.model.spec.ActionProgressDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionProgressDialogMode
import com.xiaoyv.workflow.model.spec.ActionSelectDialogConfigKey
import com.xiaoyv.workflow.model.spec.ActionShareConfigKey
import com.xiaoyv.workflow.model.spec.ActionSyncCookieConfigKey
import com.xiaoyv.workflow.model.spec.ActionToastConfigKey
import com.xiaoyv.workflow.model.spec.ActionVideoPreviewConfigKey
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * 宿主交互、系统功能与 UI 弹窗副作用节点样例集合（workflow-node-action）。
 */
internal object ActionSamples {
    val all: List<ActionWorkflow> = buildList {
        // 1. 提示与剪贴板
        add(linear("action_show_toast", "显示 Toast 提示", ActionNodeType.SHOW_TOAST, config(ActionToastConfigKey.MESSAGE to "工作流执行成功！")))
        add(
            linear(
                "action_write_clipboard",
                "写入剪贴板",
                ActionNodeType.WRITE_CLIPBOARD,
                config(ActionClipboardConfigKey.TEXT to "Workflow CMP Multiplatform"),
                setOf(ActionCapability.CLIPBOARD_WRITE),
            )
        )
        add(
            linear(
                "action_read_clipboard",
                "读取剪贴板",
                ActionNodeType.READ_CLIPBOARD,
                config(ActionClipboardConfigKey.OUTPUT_KEY to "clipContent"),
            )
        )

        // 2. 外部链接与应用
        add(
            linear(
                "action_open_external_url",
                "打开系统外部浏览器",
                ActionNodeType.OPEN_EXTERNAL_URL,
                config(ActionOpenUrlConfigKey.URL to "https://bgm.tv/subject/633836"),
                setOf(ActionCapability.OPEN_EXTERNAL_URL),
            )
        )
        add(
            linear(
                "action_open_external_app",
                "打开外部 App (URI Scheme)",
                ActionNodeType.OPEN_EXTERNAL_APP,
                config(ActionOpenAppConfigKey.URI to "market://details?id=com.xiaoyv.bangumi.multiplatform"),
                setOf(ActionCapability.OPEN_EXTERNAL_APP),
            )
        )
        add(
            linear(
                "action_open_internal_web",
                "打开内置 WebView 页面",
                ActionNodeType.OPEN_INTERNAL_WEB,
                config(ActionOpenWebConfigKey.URL to "https://hanime1.me"),
                setOf(ActionCapability.OPEN_INTERNAL_WEB),
            )
        )
        add(
            linear(
                "action_sync_cookie",
                "同步 WebView Cookie 到引擎",
                ActionNodeType.SYNC_COOKIE,
                config(
                    ActionSyncCookieConfigKey.URL to "https://hanime1.me",
                    ActionSyncCookieConfigKey.TITLE to "同步 hanime1 Cookie",
                ),
                setOf(ActionCapability.NETWORK_COOKIE_SYNC),
            )
        )

        // 3. 交互弹窗
        add(
            linear(
                "ui_confirm",
                "二次确认弹窗",
                ActionNodeType.UI_CONFIRM,
                config(ActionConfirmConfigKey.TITLE to "温馨提示", ActionConfirmConfigKey.MESSAGE to "是否确认执行此操作？"),
                setOf(ActionCapability.CONFIRM_DIALOG),
            )
        )
        add(
            linear(
                "ui_input_dialog",
                "文本输入框弹窗",
                ActionNodeType.UI_INPUT_DIALOG,
                config(
                    ActionInputDialogConfigKey.TITLE to "请输入条目备注",
                    ActionInputDialogConfigKey.SUBTITLE to "支持输入多行文本",
                    ActionInputDialogConfigKey.OUTPUT_KEY to "userNote",
                ),
                setOf(ActionCapability.INPUT_DIALOG),
            )
        )
        add(
            linear(
                "ui_select_dialog_single",
                "单选列表弹窗",
                ActionNodeType.UI_SELECT_DIALOG,
                buildJsonObject {
                    put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("请选择追番状态"))
                    put(ActionSelectDialogConfigKey.SUBTITLE, JsonPrimitive("点击任意选项即可选中并提交"))
                    put(ActionSelectDialogConfigKey.IS_MULTI_SELECT, JsonPrimitive(false))
                    put(
                        ActionSelectDialogConfigKey.OPTIONS,
                        buildJsonArray {
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("想看"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("wish"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("看过"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("collect"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("在看"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("do"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("搁置"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("on_hold"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("抛弃"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("dropped"))
                            })
                        },
                    )
                    put(ActionSelectDialogConfigKey.DEFAULT_VALUES, buildJsonArray { add(JsonPrimitive("collect")) })
                    put(ActionSelectDialogConfigKey.OUTPUT_KEY, JsonPrimitive("selectedStatus"))
                },
                setOf(ActionCapability.SELECT_DIALOG),
            )
        )
        add(
            linear(
                "ui_select_dialog_multi",
                "多选列表弹窗",
                ActionNodeType.UI_SELECT_DIALOG,
                buildJsonObject {
                    put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("请选择标签"))
                    put(ActionSelectDialogConfigKey.SUBTITLE, JsonPrimitive("支持多选，勾选完成后点击确定"))
                    put(ActionSelectDialogConfigKey.IS_MULTI_SELECT, JsonPrimitive(true))
                    put(
                        ActionSelectDialogConfigKey.OPTIONS,
                        buildJsonArray {
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("原创"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("original"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("搞笑"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("comedy"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("日常"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("slice_of_life"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("音乐"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("music"))
                            })
                        },
                    )
                    put(
                        ActionSelectDialogConfigKey.DEFAULT_VALUES,
                        buildJsonArray {
                            add(JsonPrimitive("original"))
                            add(JsonPrimitive("music"))
                        },
                    )
                    put(ActionSelectDialogConfigKey.OUTPUT_KEY, JsonPrimitive("selectedTags"))
                },
                setOf(ActionCapability.SELECT_DIALOG),
            )
        )
        add(
            linear(
                "ui_select_dialog_index",
                "列表选择 (输出 Index 模式)",
                ActionNodeType.UI_SELECT_DIALOG,
                buildJsonObject {
                    put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("请选择排序模式"))
                    put(ActionSelectDialogConfigKey.IS_MULTI_SELECT, JsonPrimitive(false))
                    put(
                        ActionSelectDialogConfigKey.OPTIONS,
                        buildJsonArray {
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("按热度排序"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("hot"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("按评分排序"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("rating"))
                            })
                            add(buildJsonObject {
                                put(ActionSelectDialogConfigKey.TITLE, JsonPrimitive("按时间排序"))
                                put(ActionSelectDialogConfigKey.VALUE, JsonPrimitive("date"))
                            })
                        },
                    )
                    put(ActionSelectDialogConfigKey.DEFAULT_INDICES, buildJsonArray { add(JsonPrimitive(1)) })
                    put(ActionSelectDialogConfigKey.OUTPUT_KEY, JsonPrimitive("selectedIndex"))
                },
                setOf(ActionCapability.SELECT_DIALOG),
            )
        )

        // 4. 进度弹窗与更新
        add(progressDialogLifecycleSample())
        add(progressDialogLoopSample())
        add(progressDialogParallelSample())
        add(progressDialogFiveBranchParallelSample())

        // 5. 媒体预览
        add(
            linear(
                "image_preview_single",
                "单图片预览",
                ActionNodeType.IMAGE_PREVIEW,
                config(
                    ActionImagePreviewConfigKey.INDEX to 0,
                    ActionImagePreviewConfigKey.IMAGES to JsonArray(listOf(JsonPrimitive("https://lain.bgm.tv/pic/photo/l/47/7e/837364_do644.jpg"))),
                ),
                setOf(ActionCapability.IMAGE_PREVIEW),
            )
        )
        add(
            linear(
                "image_preview_gallery",
                "多图画廊预览",
                ActionNodeType.IMAGE_PREVIEW,
                config(
                    ActionImagePreviewConfigKey.INDEX to 0,
                    ActionImagePreviewConfigKey.IMAGES to JsonArray(
                        listOf(
                            JsonPrimitive("https://lain.bgm.tv/pic/photo/l/47/7e/837364_do644.jpg"),
                            JsonPrimitive("https://lain.bgm.tv/pic/cover/l/7e/00/633836_8p8p1.jpg"),
                            JsonPrimitive("https://lain.bgm.tv/pic/crt/l/49/7f/126938_crt_9eR33.jpg"),
                        )
                    ),
                ),
                setOf(ActionCapability.IMAGE_PREVIEW),
            )
        )
        add(
            linear(
                "video_preview",
                "视频播放预览 (带 HTTP Headers)",
                ActionNodeType.VIDEO_PREVIEW,
                config(
                    ActionVideoPreviewConfigKey.URL to "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/uni-app-video-courses.mp4",
                    ActionVideoPreviewConfigKey.HEADERS to buildJsonObject {
                        put("User-Agent", JsonPrimitive("Mozilla/5.0"))
                    },
                ),
                setOf(ActionCapability.VIDEO_PREVIEW),
            )
        )

        // 6. 系统功能
        add(
            linear(
                "system_share",
                "系统分享面板",
                ActionNodeType.SYSTEM_SHARE,
                config(ActionShareConfigKey.TEXT to "Workflow CMP - 跨平台工作流引擎"),
                setOf(ActionCapability.SHARE),
            )
        )
        add(
            linear(
                "system_notification",
                "发送系统状态栏通知",
                ActionNodeType.SYSTEM_NOTIFICATION,
                config(ActionNotificationConfigKey.CONTENT to "您订阅的工作流任务已全部完成"),
                setOf(ActionCapability.NOTIFICATION),
            )
        )
        add(linear("system_vibrate", "触发设备触觉震动", ActionNodeType.SYSTEM_VIBRATE))
    }

    /**
     * 进度弹窗生命周期样例：创建 -> 延时 -> 刷新进度 -> 关闭。
     */
    private fun progressDialogLifecycleSample(): ActionWorkflow = workflow(
        id = "ui_progress_dialog_lifecycle",
        name = "进度弹窗：生命周期演示",
        description = "展示进度弹窗创建、动态更新进度文本及百分比、最后主动关闭的完整流程。",
        capabilities = setOf(ActionCapability.PROGRESS_DIALOG),
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "show_progress",
                ActionNodeType.UI_PROGRESS_DIALOG,
                "展示进度弹窗",
                config(
                    ActionProgressDialogConfigKey.TITLE to "正在下载资源",
                    ActionProgressDialogConfigKey.MESSAGE to "连接服务器中 (30%)...",
                    ActionProgressDialogConfigKey.MODE to ActionProgressDialogMode.DETERMINATE,
                    ActionProgressDialogConfigKey.PROGRESS to 30,
                    ActionProgressDialogConfigKey.MAX_PROGRESS to 100,
                ),
            ),
            node("delay_1", ActionNodeType.FLOW_DELAY, "等待 1 秒", config(ActionFlowConfigKey.DELAY_MILLIS to 1000)),
            node(
                "update_progress",
                ActionNodeType.UI_PROGRESS_UPDATE,
                "更新进度",
                config(
                    ActionProgressDialogConfigKey.MESSAGE to "写入本地文件中 (85%)...",
                    ActionProgressDialogConfigKey.PROGRESS to 85,
                ),
            ),
            node("delay_2", ActionNodeType.FLOW_DELAY, "等待 1 秒", config(ActionFlowConfigKey.DELAY_MILLIS to 1000)),
            node("dismiss_progress", ActionNodeType.UI_PROGRESS_DISMISS, "关闭弹窗", config()),
            node("finish_toast", ActionNodeType.SHOW_TOAST, "完成提示", config(ActionToastConfigKey.MESSAGE to "资源下载并解析完毕！")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "show_progress"),
            edge("show_progress", ActionControlPortId.SUCCESS, "delay_1"),
            edge("delay_1", ActionControlPortId.NEXT, "update_progress"),
            edge("update_progress", ActionControlPortId.SUCCESS, "delay_2"),
            edge("delay_2", ActionControlPortId.NEXT, "dismiss_progress"),
            edge("dismiss_progress", ActionControlPortId.SUCCESS, "finish_toast"),
            edge("finish_toast", ActionControlPortId.SUCCESS, "end"),
        ),
    )

    /**
     * 进度弹窗循环动态刷新样例：在 loop.repeat 循环中实时累加百分比并更新 UI。
     */
    private fun progressDialogLoopSample(): ActionWorkflow = workflow(
        id = "ui_progress_dialog_loop",
        name = "进度弹窗：循环动态累加刷新",
        description = "在循环内部每 100ms 动态累加 10% 进度并实时刷新弹窗，直到 100% 后关闭。",
        capabilities = setOf(ActionCapability.PROGRESS_DIALOG),
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node("init_val", ActionNodeType.SET_VARIABLE, "初始化进度", config(ActionDataConfigKey.KEY to "progress", ActionDataConfigKey.VALUE to 0)),
            node(
                "show_progress",
                ActionNodeType.UI_PROGRESS_DIALOG,
                "展示进度弹窗",
                config(
                    ActionProgressDialogConfigKey.TITLE to "批量转换数据",
                    ActionProgressDialogConfigKey.MESSAGE to "准备开始...",
                    ActionProgressDialogConfigKey.MODE to ActionProgressDialogMode.DETERMINATE,
                    ActionProgressDialogConfigKey.PROGRESS to 0,
                    ActionProgressDialogConfigKey.MAX_PROGRESS to 100,
                ),
            ),
            node("loop", ActionNodeType.LOOP_REPEAT, "循环 10 次", config(ActionLoopConfigKey.COUNT to 10, ActionLoopConfigKey.MAX_ITERATIONS to 20)),
            node("delay", ActionNodeType.FLOW_DELAY, "模拟 100ms 耗时", config(ActionFlowConfigKey.DELAY_MILLIS to 100)),
            node("add_progress", ActionNodeType.SET_VARIABLE, "进度加 10", config(ActionDataConfigKey.KEY to "progress", ActionDataConfigKey.VALUE to "\${vars.progress + 10}")),
            node(
                "update_ui",
                ActionNodeType.UI_PROGRESS_UPDATE,
                "刷新弹窗 UI",
                config(ActionProgressDialogConfigKey.MESSAGE to "已完成 \${vars.progress}%", ActionProgressDialogConfigKey.PROGRESS to "\${vars.progress}")
            ),
            node("loop_next", ActionNodeType.LOOP_NEXT, "下一次循环", config(ActionLoopConfigKey.LOOP_ID to "loop")),
            node("dismiss_progress", ActionNodeType.UI_PROGRESS_DISMISS, "关闭弹窗", config()),
            node("finish_toast", ActionNodeType.SHOW_TOAST, "完成提示", config(ActionToastConfigKey.MESSAGE to "批量转换已全部完成 (100%)！")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "init_val"),
            edge("init_val", ActionControlPortId.NEXT, "show_progress"),
            edge("show_progress", ActionControlPortId.SUCCESS, "loop"),
            edge("loop", ActionControlPortId.BODY, "delay"),
            edge("delay", ActionControlPortId.NEXT, "add_progress"),
            edge("add_progress", ActionControlPortId.NEXT, "update_ui"),
            edge("update_ui", ActionControlPortId.SUCCESS, "loop_next"),
            edge("loop", ActionControlPortId.COMPLETED, "dismiss_progress"),
            edge("dismiss_progress", ActionControlPortId.SUCCESS, "finish_toast"),
            edge("finish_toast", ActionControlPortId.SUCCESS, "end"),
        ),
    )

    /**
     * 进度弹窗：5 条并发分支各自展示弹窗，分 5 轮更新至 100% 后关闭，
     * 最后通过 flow.join 汇合退出。
     */
    private fun progressDialogFiveBranchParallelSample(): ActionWorkflow = workflow(
        id = "ui_progress_dialog_5_branch_parallel",
        name = "进度弹窗：5 分支并发进度验证",
        description = "使用 flow.parallel 同时启动 5 条分支；每条分支以独立 taskId 展示 0/100 进度弹窗，" +
                "每秒更新 20%，共 5 轮至 100% 后关闭，最终在 flow.join 汇合退出。",
        capabilities = setOf(ActionCapability.PROGRESS_DIALOG),
        nodes = buildList {
            add(node("start", ActionNodeType.FLOW_START, "开始"))
            add(node("parallel", ActionNodeType.FLOW_PARALLEL, "启动 5 条并发分支"))
            for (taskId in 1..5) {
                add(node("branch_${taskId}_show", ActionNodeType.UI_PROGRESS_DIALOG, "分支$taskId 展示进度弹窗", progressDialogConfig(taskId)))
                for (round in 1..5) {
                    val progress = round * 20
                    add(
                        node(
                            "branch_${taskId}_delay_$round",
                            ActionNodeType.FLOW_DELAY,
                            "分支$taskId 第${round}轮延时 1s",
                            config(ActionFlowConfigKey.DELAY_MILLIS to 1_000L, ActionProgressDialogConfigKey.TASK_ID to taskId)
                        )
                    )
                    add(node("branch_${taskId}_update_$round", ActionNodeType.UI_PROGRESS_UPDATE, "分支$taskId 更新至 $progress%", progressUpdateConfig(taskId, progress)))
                }
                add(node("branch_${taskId}_dismiss", ActionNodeType.UI_PROGRESS_DISMISS, "分支$taskId 关闭进度弹窗", config(ActionProgressDialogConfigKey.TASK_ID to taskId)))
            }
            add(
                node(
                    "join",
                    ActionNodeType.FLOW_JOIN,
                    "等待全部 5 条分支完成",
                    config(ActionFlowConfigKey.VALUES to JsonArray(emptyList()), ActionFlowConfigKey.OUTPUT_KEY to "joinResult")
                )
            )
            add(node("end", ActionNodeType.FLOW_END, "结束"))
        },
        edges = buildList {
            add(edge("start", ActionControlPortId.NEXT, "parallel"))
            for (taskId in 1..5) {
                add(edge("parallel", ActionControlPortId.BRANCHES, "branch_${taskId}_show"))
                add(edge("branch_${taskId}_show", ActionControlPortId.SUCCESS, "branch_${taskId}_delay_1"))
                for (round in 1..5) {
                    add(edge("branch_${taskId}_delay_$round", ActionControlPortId.NEXT, "branch_${taskId}_update_$round"))
                    val nextNodeId = if (round == 5) "branch_${taskId}_dismiss" else "branch_${taskId}_delay_${round + 1}"
                    add(edge("branch_${taskId}_update_$round", ActionControlPortId.SUCCESS, nextNodeId))
                }
                add(edge("branch_${taskId}_dismiss", ActionControlPortId.SUCCESS, "join"))
            }
            add(edge("join", ActionControlPortId.NEXT, "end"))
        },
    )

    private fun progressDialogConfig(taskId: Int) = config(
        ActionProgressDialogConfigKey.TITLE to "并发任务 $taskId",
        ActionProgressDialogConfigKey.MESSAGE to "任务 $taskId 进行中...",
        ActionProgressDialogConfigKey.MODE to ActionProgressDialogMode.DETERMINATE,
        ActionProgressDialogConfigKey.PROGRESS to 0,
        ActionProgressDialogConfigKey.MAX_PROGRESS to 100,
        ActionProgressDialogConfigKey.TASK_ID to taskId,
    )

    private fun progressUpdateConfig(taskId: Int, progress: Int) = config(
        ActionProgressDialogConfigKey.MESSAGE to "任务 $taskId 已完成 $progress%",
        ActionProgressDialogConfigKey.PROGRESS to progress,
        ActionProgressDialogConfigKey.TASK_ID to taskId,
    )

    /**
     * 进度弹窗并发多分支刷新与合流样例。
     */
    private fun progressDialogParallelSample(): ActionWorkflow = workflow(
        id = "ui_progress_dialog_parallel_sample",
        name = "进度弹窗：并发分支刷新与汇合",
        description = "使用 flow.parallel 并发执行两条异步任务分支，各自刷新进度，最后在 flow.join 汇合后关闭弹窗。",
        capabilities = setOf(ActionCapability.PROGRESS_DIALOG),
        nodes = listOf(
            node("start", ActionNodeType.FLOW_START, "开始"),
            node(
                "show_progress",
                ActionNodeType.UI_PROGRESS_DIALOG,
                "展示并发下载弹窗",
                config(
                    ActionProgressDialogConfigKey.TITLE to "音视频并发下载",
                    ActionProgressDialogConfigKey.MESSAGE to "启动并发下载任务...",
                    ActionProgressDialogConfigKey.MODE to ActionProgressDialogMode.DETERMINATE,
                    ActionProgressDialogConfigKey.PROGRESS to 0,
                    ActionProgressDialogConfigKey.MAX_PROGRESS to 100,
                ),
            ),
            node("parallel", ActionNodeType.FLOW_PARALLEL, "启动并发任务"),
            node("download_video", ActionNodeType.FLOW_DELAY, "下载视频流 (500ms)", config(ActionFlowConfigKey.DELAY_MILLIS to 500)),
            node(
                "update_video",
                ActionNodeType.UI_PROGRESS_UPDATE,
                "更新视频进度",
                config(ActionProgressDialogConfigKey.MESSAGE to "视频流下载完成 (50%)", ActionProgressDialogConfigKey.PROGRESS to 50)
            ),
            node("download_audio", ActionNodeType.FLOW_DELAY, "下载音频流 (1000ms)", config(ActionFlowConfigKey.DELAY_MILLIS to 1000)),
            node(
                "update_audio",
                ActionNodeType.UI_PROGRESS_UPDATE,
                "更新音频进度",
                config(ActionProgressDialogConfigKey.MESSAGE to "音频与字幕下载完成 (90%)", ActionProgressDialogConfigKey.PROGRESS to 90)
            ),
            node("join", ActionNodeType.FLOW_JOIN, "等待全部完成", config(ActionFlowConfigKey.VALUES to JsonArray(emptyList()), ActionFlowConfigKey.OUTPUT_KEY to "joinResult")),
            node(
                "update_complete",
                ActionNodeType.UI_PROGRESS_UPDATE,
                "完成合并",
                config(ActionProgressDialogConfigKey.MESSAGE to "音视频合成完成 (100%)", ActionProgressDialogConfigKey.PROGRESS to 100)
            ),
            node("delay_finish", ActionNodeType.FLOW_DELAY, "稍作停留", config(ActionFlowConfigKey.DELAY_MILLIS to 300)),
            node("dismiss_progress", ActionNodeType.UI_PROGRESS_DISMISS, "关闭进度", config()),
            node("finish_toast", ActionNodeType.SHOW_TOAST, "完成提示", config(ActionToastConfigKey.MESSAGE to "全部音视频资源下载并合并完成！")),
            node("end", ActionNodeType.FLOW_END, "结束"),
        ),
        edges = listOf(
            edge("start", ActionControlPortId.NEXT, "show_progress"),
            edge("show_progress", ActionControlPortId.SUCCESS, "parallel"),
            edge("parallel", ActionControlPortId.BRANCHES, "download_video"),
            edge("parallel", ActionControlPortId.BRANCHES, "download_audio"),
            edge("download_video", ActionControlPortId.NEXT, "update_video"),
            edge("update_video", ActionControlPortId.SUCCESS, "join"),
            edge("download_audio", ActionControlPortId.NEXT, "update_audio"),
            edge("update_audio", ActionControlPortId.SUCCESS, "join"),
            edge("join", ActionControlPortId.NEXT, "update_complete"),
            edge("update_complete", ActionControlPortId.SUCCESS, "delay_finish"),
            edge("delay_finish", ActionControlPortId.NEXT, "dismiss_progress"),
            edge("dismiss_progress", ActionControlPortId.SUCCESS, "finish_toast"),
            edge("finish_toast", ActionControlPortId.SUCCESS, "end"),
        ),
    )
}
