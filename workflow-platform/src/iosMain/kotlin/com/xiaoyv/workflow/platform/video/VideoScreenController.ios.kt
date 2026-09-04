package com.xiaoyv.workflow.platform.video

class IosVideoScreenController : VideoScreenController {
    override fun enterPortrait() = Unit

    override fun enterFullscreen() = Unit
}

fun VideoScreenController(): VideoScreenController = IosVideoScreenController()
