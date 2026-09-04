package com.xiaoyv.workflow.util

import kotlinx.serialization.json.Json

/**
 * 工作流协议使用的宽松 JSON 编解码器。
 */
val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}
