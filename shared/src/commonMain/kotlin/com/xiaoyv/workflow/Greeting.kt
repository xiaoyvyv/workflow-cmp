package com.xiaoyv.workflow

import com.xiaoyv.workflow.platform.info.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}