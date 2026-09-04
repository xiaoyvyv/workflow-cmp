plugins {
    id("workflow.library")
    id("workflow.native")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.platform"
    }

    sourceSets {
        commonTest.dependencies {
        }
        commonMain.dependencies { }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }

        iosMain.dependencies { }

        iosMain.dependencies { }
    }
}
