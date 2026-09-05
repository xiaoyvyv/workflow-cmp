plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.editor.mcp"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.workflowCore)
            api(projects.workflowEditorBridge)
            implementation(libs.bundles.kotlinx)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.immutable)
        }
    }
}
