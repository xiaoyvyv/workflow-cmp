plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.control"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowNodeBase)
            implementation(projects.workflowNodeData)
        }
        commonTest.dependencies {
            implementation(projects.workflowNodeTestkit)
            implementation(projects.workflowNodeAll)
        }
    }
}
