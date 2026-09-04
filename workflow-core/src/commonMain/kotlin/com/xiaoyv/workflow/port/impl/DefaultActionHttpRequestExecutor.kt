package com.xiaoyv.workflow.port.impl

import com.xiaoyv.workflow.model.spec.ActionHttpBodyType
import com.xiaoyv.workflow.model.spec.ActionHttpResponseKey
import com.xiaoyv.workflow.node.effect.ActionHttpRequestEffect
import com.xiaoyv.workflow.port.ActionHttpRequestExecutor
import com.xiaoyv.workflow.util.defaultJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

/**
 * 工作流内置 HTTP 请求执行器。
 *
 * 使用应用统一配置的 Ktor 客户端执行请求，并将状态码与解析后的 JSON 响应作为节点输出返回。
 *
 * @param hostCookieStorge 宿主的 Cookie 库，仅支持访问且需要严格控制权限；
 */
class DefaultActionHttpRequestExecutor(
    private val httpClient: HttpClient,
    private val hostCookieStorge: CookiesStorage = EmptyActionCookiesStorage,
) : ActionHttpRequestExecutor {
    /**
     * 不需要 Cookie 的 HttpClient
     */
    private val anonymousHttpClient by lazy {
        httpClient.config {
            install(HttpCookies) {
                storage = EmptyActionCookiesStorage
            }
        }
    }

    private val ActionHttpRequestEffect.targetClient: HttpClient
        get() = if (useLocalCookieStorage) httpClient else anonymousHttpClient

    /**
     * 执行请求并返回可写入 `steps.<nodeId>` 的标准输出。
     *
     * @param request HTTP 请求副作用。
     */
    override suspend fun execute(request: ActionHttpRequestEffect): JsonObject {
        var lastFailure: Throwable? = null
        repeat(request.retryCount.coerceAtLeast(0) + 1) { attempt ->
            try {
                val output = executeOnce(request)
                val statusCode = output[ActionHttpResponseKey.STATUS_CODE]?.let { (it as JsonPrimitive).content.toInt() } ?: 0
                if (statusCode !in 500..599 || attempt >= request.retryCount) return output
                if (request.retryDelayMillis > 0) delay(request.retryDelayMillis.milliseconds)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                lastFailure = throwable
                if (attempt >= request.retryCount) throw throwable
                if (request.retryDelayMillis > 0) delay(request.retryDelayMillis.milliseconds)
            }
        }
        throw checkNotNull(lastFailure)
    }

    override suspend fun download(
        request: ActionHttpRequestEffect,
        onResponse: suspend (ActionHttpRequestExecutor.ActionHttpDownloadResponse) -> Unit,
        consumeChunk: suspend (ByteArray) -> Unit,
    ): ActionHttpRequestExecutor.ActionHttpDownloadResponse {
        var lastFailure: Throwable? = null
        repeat(request.retryCount.coerceAtLeast(0) + 1) { attempt ->
            try {
                val output = requestStatement(
                    request = request,
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS,
                ).execute { response ->
                    val responseOutput = ActionHttpRequestExecutor.ActionHttpDownloadResponse(
                        statusCode = response.status.value,
                        contentType = response.headers[HttpHeaders.ContentType].orEmpty(),
                        contentDisposition = response.headers[HttpHeaders.ContentDisposition],
                    )
                    if (responseOutput.statusCode in 500..599 && attempt < request.retryCount) {
                        return@execute responseOutput
                    }
                    onResponse(responseOutput)
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                    while (true) {
                        val count = channel.readAvailable(buffer)
                        if (count <= 0) break
                        consumeChunk(buffer.copyOf(count))
                    }
                    responseOutput
                }
                if (output.statusCode in 500..599 && attempt < request.retryCount) {
                    if (request.retryDelayMillis > 0) delay(request.retryDelayMillis.milliseconds)
                    return@repeat
                }
                return output
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                lastFailure = throwable
                if (attempt >= request.retryCount) throw throwable
                if (request.retryDelayMillis > 0) delay(request.retryDelayMillis.milliseconds)
            }
        }
        throw checkNotNull(lastFailure)
    }

    /**
     * 执行一次 HTTP 请求，并规范化响应体。
     */
    private suspend fun executeOnce(request: ActionHttpRequestEffect): JsonObject {
        val response = requestOnce(request)
        val rawBody: String = response.body()
        val responseContentType = response.headers[HttpHeaders.ContentType].orEmpty()
        val body = when {
            responseContentType.contains("json", ignoreCase = true) -> {
                runCatching { defaultJson.parseToJsonElement(rawBody) }.getOrElse { JsonPrimitive(rawBody) }
            }

            responseContentType.contains("html", ignoreCase = true) || rawBody.trimStart().startsWith("<") ->
                JsonPrimitive(rawBody)

            else -> JsonPrimitive(rawBody)
        }

        return buildJsonObject {
            put(ActionHttpResponseKey.STATUS_CODE, JsonPrimitive(response.status.value))
            put(ActionHttpResponseKey.IS_SUCCESS, JsonPrimitive(response.status.value in 200..299))
            put(ActionHttpResponseKey.CONTENT_TYPE, JsonPrimitive(responseContentType))
            put(ActionHttpResponseKey.RAW_BODY, JsonPrimitive(rawBody))
            put(ActionHttpResponseKey.BODY, body)
        }
    }

    private suspend fun requestOnce(request: ActionHttpRequestEffect) =
        request.targetClient.request(request.url) {
            configureRequest(request, request.timeoutMillis)
        }

    private suspend fun requestStatement(
        request: ActionHttpRequestEffect,
        requestTimeoutMillis: Long? = request.timeoutMillis
    ) = request.targetClient.prepareRequest(request.url) {
        configureRequest(request, requestTimeoutMillis)
    }

    private fun HttpRequestBuilder.configureRequest(
        request: ActionHttpRequestEffect,
        requestTimeoutMillis: Long?,
    ) {
        method = HttpMethod.parse(request.method)
        url { request.query.forEach { (key, value) -> parameters.append(key, value.toString().trim('"')) } }
        request.headers.forEach { (key, value) ->
            val headerValue = value.jsonPrimitive.contentOrNull ?: error("HTTP 请求头 [$key] 的值不能为空")
            headers.append(key, headerValue)
        }
        requestTimeoutMillis?.let { timeout { this.requestTimeoutMillis = it } }
        if (request.body !is JsonNull) {
            val encoded = when (request.bodyType) {
                ActionHttpBodyType.FORM_URL_ENCODED -> Parameters.build {
                    val form = request.body as? JsonObject ?: error("formUrlEncoded 请求体必须是 JSON 对象")
                    form.forEach { (key, value) ->
                        append(key, value.toString().trim('"'))
                    }
                }.formUrlEncode()

                ActionHttpBodyType.TEXT -> request.body.toString().trim('"')
                else -> request.body.toString()
            }
            contentType(
                ContentType.parse(
                    request.contentType ?: when (request.bodyType) {
                        ActionHttpBodyType.FORM_URL_ENCODED -> ContentType.Application.FormUrlEncoded.toString()
                        ActionHttpBodyType.TEXT -> ContentType.Text.Plain.toString()
                        else -> ContentType.Application.Json.toString()
                    },
                ),
            )
            setBody(encoded)
        }
    }

    private companion object {
        const val DOWNLOAD_BUFFER_SIZE = 256 * 1024
    }

    private object EmptyActionCookiesStorage : CookiesStorage {
        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = Unit

        override suspend fun get(requestUrl: Url): List<Cookie> = emptyList()

        override fun close() = Unit
    }
}
