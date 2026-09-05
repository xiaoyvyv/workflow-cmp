plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.editor.bridge.contract"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.workflowCore)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
