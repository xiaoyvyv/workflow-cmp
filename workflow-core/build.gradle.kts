plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.core"
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
