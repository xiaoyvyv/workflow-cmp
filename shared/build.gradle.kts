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

            implementation(projects.workflowUiCore)
            implementation(projects.workflowUiAll)

            implementation(libs.compose.kuiver)
            implementation(libs.compose.lifecycle.viewmodel)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
    packageOfResClass = "com.xiaoyv.workflow.$composeResourceId.resources"
}
