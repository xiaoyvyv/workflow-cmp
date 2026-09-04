plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.platform.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowPlatform)
            implementation(projects.workflowCore)
            implementation(projects.workflowNodeIo)
        }

        iosMain.dependencies { }

        iosMain.dependencies { }
    }
}
