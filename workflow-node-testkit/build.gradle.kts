plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.testkit"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowPlatform)
            implementation(projects.workflowNodeAll)
            implementation(projects.workflowNodeControl)
            implementation(projects.workflowNodeData)
            implementation(projects.workflowNodeCodec)
            implementation(projects.workflowNodeHtml)
            implementation(projects.workflowNodeCrypto)
            implementation(projects.workflowNodeIo)
            implementation(projects.workflowNodeBilibili)
        }

        commonTest.dependencies {
            implementation(projects.workflowNodeAll)
        }
    }
}
