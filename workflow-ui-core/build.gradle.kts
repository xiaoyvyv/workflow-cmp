plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.core"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowPlatformUi)
        }
    }
}
