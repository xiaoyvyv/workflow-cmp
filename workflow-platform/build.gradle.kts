plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.platform"
    }

    sourceSets {
        commonMain.dependencies {

        }

        val skikoMain = create("skikoMain") {
            dependsOn(commonMain.get())
        }
        iosMain.get().dependsOn(skikoMain)
        jvmMain.get().dependsOn(skikoMain)
    }
}
