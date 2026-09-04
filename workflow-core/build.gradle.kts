plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.core"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowPlatform)

            api(libs.kmp.zip)
            api(libs.bundles.file.kit)
            api(libs.bundles.datastore)
        }

        commonTest.dependencies {
            implementation(projects.workflowNodeAll)
        }
    }
}
