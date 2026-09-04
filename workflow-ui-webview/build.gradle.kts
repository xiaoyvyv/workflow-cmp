plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.webview"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowUiCore)
            implementation(projects.workflowPlatformUi)
            implementation(libs.compose.webview)
        }
    }
}
