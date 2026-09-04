plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.video"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowUiCore)
            implementation(projects.workflowPlatform)
            implementation(projects.workflowPlatformUi)
            implementation(libs.compose.mediamp.all)
        }
    }
}
