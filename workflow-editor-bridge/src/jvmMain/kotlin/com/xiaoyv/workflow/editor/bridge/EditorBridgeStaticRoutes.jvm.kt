package com.xiaoyv.workflow.editor.bridge

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route

actual fun Route.installStaticEditorRoutes() {
    staticResources("/", "") { default("index.html") }
}
