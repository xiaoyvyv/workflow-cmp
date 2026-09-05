package com.xiaoyv.workflow.editor.bridge

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.fromFileExtension
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

/**
 * iOS / Native 平台静态 Web 资源托管实现。
 * 从 App Bundle 中动态查找并响应 Web 编辑器静态资产（HTML、JS、CSS 等）。
 */
@OptIn(ExperimentalForeignApi::class)
actual fun Route.installStaticEditorRoutes() {
    get("/") {
        serveBundleFile("index.html", call)
    }
    get("/{staticPath...}") {
        val rawPath = call.parameters.getAll("staticPath")?.joinToString("/").orEmpty()
        val path = if (rawPath.isBlank()) "index.html" else rawPath
        serveBundleFile(path, call)
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun serveBundleFile(
    relativePath: String,
    call: io.ktor.server.application.ApplicationCall,
) {
    val bundle = NSBundle.mainBundle
    val basePath = bundle.resourcePath ?: bundle.bundlePath
    val candidatePaths = listOf(
        "$basePath/$relativePath",
        "$basePath/workflow-editor-web/$relativePath",
    )

    val fileManager = NSFileManager.defaultManager
    val targetPath = candidatePaths.firstOrNull { fileManager.fileExistsAtPath(it) }

    if (targetPath != null) {
        val data = NSData.dataWithContentsOfFile(targetPath)
        if (data != null && data.length > 0u) {
            val bytes = ByteArray(data.length.toInt())
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
            val ext = targetPath.substringAfterLast('.', "")
            val contentType = ContentType.fromFileExtension(ext).firstOrNull()
                ?: ContentType.Application.OctetStream
            call.respondBytes(bytes, contentType)
            return
        }
    }
    call.respond(HttpStatusCode.NotFound)
}
