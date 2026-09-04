plugins {
    id("workflow.library")
    id("androidx.room")
    id("com.google.devtools.ksp")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.platform.room"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.workflowCore)
            api(libs.androidx.room.runtime)
            api(libs.androidx.sqlite.bundled)
            api(libs.bundles.file.kit)
        }

        androidMain.dependencies {
            implementation(libs.androidx.room3.sqlite.wrapper)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
}

