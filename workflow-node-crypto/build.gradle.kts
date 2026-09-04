plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.crypto"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowNodeBase)
            implementation(libs.cryptohash)


        }


    }
}
