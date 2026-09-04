plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowNodeBase)
        }
        commonTest.dependencies {
            implementation(projects.workflowNodeTestkit)
        }
    }
}
