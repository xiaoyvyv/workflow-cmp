plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.all"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowUi)
            implementation(projects.workflowUiSideEffect)
            implementation(projects.workflowUiImage)
            implementation(projects.workflowUiVideo)
            implementation(projects.workflowUiWebview)
        }
    }
}
