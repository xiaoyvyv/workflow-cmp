package com.xiaoyv.workflow.platform.info

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform