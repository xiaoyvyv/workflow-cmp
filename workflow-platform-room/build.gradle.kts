plugins {
    id("workflow.library")
    id("androidx.room3")
    id("com.google.devtools.ksp")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.platform.room"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.workflowCore)
            api(libs.androidx.room3.runtime)
            api(libs.androidx.sqlite.bundled)
            api(libs.bundles.file.kit)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
}

