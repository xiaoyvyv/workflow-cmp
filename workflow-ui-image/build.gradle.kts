plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.image"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowUi)
            implementation(libs.coil3.compose)
            implementation(libs.zoomimage.coil3)


        }


    }
}
