package com.xiaoyv.workflow.platform.video

/**
 * 视频画面的方向与全屏控制器。
 */
interface VideoScreenController {
    /** 切换至竖屏窗口。 */
    fun enterPortrait()

    /** 切换至横向沉浸式全屏窗口。 */
    fun enterFullscreen()
}
