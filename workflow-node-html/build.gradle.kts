plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.html"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowNodeBase)
            implementation(libs.ksoup)


        }
        commonTest.dependencies {
            implementation(projects.workflowNodeTestkit)


        }


    }
}
