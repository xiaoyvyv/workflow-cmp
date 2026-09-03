package com.xiaoyv.workflow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform