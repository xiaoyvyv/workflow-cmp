package com.xiaoyv.workflow.editor.bridge

import io.ktor.server.routing.Route

/**
 * 平台相关的静态 Web 编辑器资源托管路由。
 */
expect fun Route.installStaticEditorRoutes()
