package com.xiaoyv.workflow.node.all

import com.xiaoyv.workflow.di.DefaultManifestExporter
import java.io.File

fun main(args: Array<String>) {
    val outputPath = args.firstOrNull() ?: "workflow-editor-web/manifest.json"
    val json = DefaultManifestExporter.generateManifestJson()
    val file = File(outputPath)
    file.parentFile?.mkdirs()
    file.writeText(json)
    println("Successfully exported default manifest to: ${file.absolutePath}")
}
