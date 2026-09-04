plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.io"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowNodeBase)
        }

        commonTest.dependencies {
            implementation(projects.workflowNodeTestkit)
            implementation(projects.workflowNodeAll)
            implementation(projects.workflowCore)
        }
    }
}
