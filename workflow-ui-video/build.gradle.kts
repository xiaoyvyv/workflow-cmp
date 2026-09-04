plugins {
    id("workflow.ui")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.ui.video"


    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowUi)
            implementation(libs.compose.mediamp.all)
        }
    }
}
