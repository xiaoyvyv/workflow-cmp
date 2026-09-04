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
            implementation(projects.workflowNodeAll)
            implementation(projects.workflowPlatform)
            implementation(projects.workflowUi)
            implementation(projects.workflowUiAll)

            implementation(libs.compose.kuiver)
            implementation(libs.compose.lifecycle.viewmodel)
            implementation(libs.compose.mvi)
            implementation(libs.compose.mvi.compose)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "com.xiaoyv.workflow.$composeResourceId.resources"
}
