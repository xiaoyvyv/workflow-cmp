plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui"


    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowNodeIo)
        }

        iosMain.dependencies {

        }

        iosMain.dependencies {

        }
    }
}
