plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.base"


    }
    sourceSets {
        commonMain.dependencies {
            api(projects.workflowCore)


        }


    }
}
