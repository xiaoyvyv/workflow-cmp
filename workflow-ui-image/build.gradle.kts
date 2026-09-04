plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.image"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowUiCore)
            implementation(projects.workflowPlatformUi)

            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor3)
            implementation(libs.zoomimage.coil3)
        }
    }
}
