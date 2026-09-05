package com.xiaoyv.workflow.model.spec

import com.xiaoyv.workflow.Immutable
import kotlinx.serialization.Serializable

/**
 * 工作流权限/能力描述规格。
 */
@Immutable
@Serializable
data class ActionCapabilitySpec(
    val id: String,
    val label: String,
    val description: String = "",
)

/**
 * 工作流需要向用户声明的高风险能力。
 */
object ActionCapability {
    const val OPEN_EXTERNAL_URL = "open_external_url"
    const val OPEN_EXTERNAL_APP = "open_external_app"
    const val OPEN_INTERNAL_WEB = "open_internal_web"
    const val NETWORK = "network"
    const val NETWORK_LOCAL_COOKIE_ACCESS = "network_local_cookie_access"
    const val NETWORK_COOKIE_SYNC = "network_cookie_sync"
    const val CLIPBOARD_WRITE = "clipboard_write"
    const val CONFIRM_DIALOG = "confirm_dialog"
    const val INPUT_DIALOG = "input_dialog"
    const val SELECT_DIALOG = "select_dialog"
    const val PROGRESS_DIALOG = "progress_dialog"
    const val SHARE = "share"
    const val NOTIFICATION = "notification"
    const val IMAGE_PREVIEW = "image_preview"
    const val VIDEO_PREVIEW = "video_preview"

    val allSpecs: Map<String, ActionCapabilitySpec> = listOf(
        ActionCapabilitySpec(
            id = NETWORK,
            label = "网络访问",
            description = "允许向外部网络或 API 发送 HTTP 请求",
        ),
        ActionCapabilitySpec(
            id = NETWORK_LOCAL_COOKIE_ACCESS,
            label = "本地 Cookie 访问",
            description = "允许读取或使用本地 Cookie 存储中的认证凭证",
        ),
        ActionCapabilitySpec(
            id = NETWORK_COOKIE_SYNC,
            label = "Cookie 同步",
            description = "允许同步或向宿主写入网络 Cookie",
        ),
        ActionCapabilitySpec(
            id = OPEN_EXTERNAL_URL,
            label = "打开外部链接",
            description = "允许调用外部浏览器打开特定网页链接",
        ),
        ActionCapabilitySpec(
            id = OPEN_EXTERNAL_APP,
            label = "打开外部应用",
            description = "允许调用系统 Scheme 或应用打开第三方 App",
        ),
        ActionCapabilitySpec(
            id = OPEN_INTERNAL_WEB,
            label = "内置网页浏览",
            description = "允许在内置 WebView 中打开并呈现网页",
        ),
        ActionCapabilitySpec(
            id = CLIPBOARD_WRITE,
            label = "剪贴板写入",
            description = "允许修改系统剪贴板内容",
        ),
        ActionCapabilitySpec(
            id = CONFIRM_DIALOG,
            label = "确认对话框",
            description = "允许向用户弹出确认/取消询问对话框",
        ),
        ActionCapabilitySpec(
            id = INPUT_DIALOG,
            label = "输入对话框",
            description = "允许向用户弹出文本输入弹窗以获取用户输入",
        ),
        ActionCapabilitySpec(
            id = SELECT_DIALOG,
            label = "选择对话框",
            description = "允许向用户弹出列表或单选对话框",
        ),
        ActionCapabilitySpec(
            id = PROGRESS_DIALOG,
            label = "进度对话框",
            description = "允许向用户显示或更新任务执行进度弹窗",
        ),
        ActionCapabilitySpec(
            id = SHARE,
            label = "系统分享",
            description = "允许调起系统原生分享面板",
        ),
        ActionCapabilitySpec(
            id = NOTIFICATION,
            label = "系统通知",
            description = "允许向系统发送通知栏消息",
        ),
        ActionCapabilitySpec(
            id = IMAGE_PREVIEW,
            label = "图片预览",
            description = "允许打开全屏图片查看器",
        ),
        ActionCapabilitySpec(
            id = VIDEO_PREVIEW,
            label = "视频预览",
            description = "允许打开视频播放器进行内容预览",
        ),
    ).associateBy { it.id }

    fun specOf(id: String): ActionCapabilitySpec =
        allSpecs[id] ?: ActionCapabilitySpec(id = id, label = id)
}

