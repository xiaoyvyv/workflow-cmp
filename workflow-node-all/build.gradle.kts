plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.all"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowPlatform)
            implementation(projects.workflowNodeBase)
            implementation(projects.workflowNodeControl)
            implementation(projects.workflowNodeData)
            implementation(projects.workflowNodeCodec)
            implementation(projects.workflowNodeHtml)
            implementation(projects.workflowNodeCrypto)
            implementation(projects.workflowNodeIo)
            implementation(projects.workflowNodeBilibili)
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.kmp.zip)
        }
    }
}
