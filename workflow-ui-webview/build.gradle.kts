plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.webview"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowUiCore)
            implementation(projects.workflowCore)
            implementation(libs.compose.webview)
        }
    }
}
