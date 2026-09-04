package com.xiaoyv.workflow.port

import com.xiaoyv.workflow.node.effect.ActionHttpRequestEffect
import kotlinx.serialization.json.JsonObject

/**
 * 工作流 HTTP 节点所需的请求能力端口。
 *
 * 工作流引擎仅依赖这个抽象，不感知 Ktor、Cookie、应用网络设置等 data 基础设施。
 * 宿主可替换实现，以适配测试、受限网络或其他平台的 HTTP 客户端。
 */
fun interface ActionHttpRequestExecutor {
    /**
     * 执行 HTTP 请求，并返回可供后续节点通过 `steps.<nodeId>` 读取的结构化输出。
     *
     * @param request HTTP 请求节点声明的请求副作用。
     * @return 标准化的 HTTP 响应输出。
     */
    suspend fun execute(request: ActionHttpRequestEffect): JsonObject

    /**
     * 通过响应通道分块读取下载内容。
     */
    suspend fun download(
        request: ActionHttpRequestEffect,
        onResponse: suspend (ActionHttpDownloadResponse) -> Unit,
        consumeChunk: suspend (ByteArray) -> Unit,
    ): ActionHttpDownloadResponse {
        error("当前 HTTP 请求执行器不支持文件下载")
    }

    /**
     * HTTP 下载响应。
     */
    data class ActionHttpDownloadResponse(
        val statusCode: Int,
        val contentType: String,
        val contentDisposition: String?,
    )
}