plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.webview"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowUi)
            implementation(projects.workflowCore)
            implementation(libs.compose.webview)
        }
    }
}
