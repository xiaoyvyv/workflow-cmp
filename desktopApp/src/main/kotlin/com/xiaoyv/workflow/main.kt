package com.xiaoyv.workflow

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workflowcmp",
    ) {
        App()
    }
}
