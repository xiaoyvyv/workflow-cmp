plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.shared"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowEditorBridge)
            implementation(projects.workflowPlatformRoom)
            implementation(projects.workflowNodeAll)

            implementation(projects.workflowUiCore)
            implementation(projects.workflowUiAll)

            implementation(libs.compose.kuiver)
            implementation(libs.compose.lifecycle.viewmodel)

            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor3)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "com.xiaoyv.workflow.$composeResourceId.resources"
}
