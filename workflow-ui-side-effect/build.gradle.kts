plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.sideeffect"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowUi)
            implementation(projects.workflowCore)
            implementation(projects.workflowUiImage)
        }
    }
}
