plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.editor.bridge"
    }

    sourceSets {
        commonMain {
            resources.srcDir("../workflow-editor-web")
            dependencies {
                api(projects.workflowCore)
                api(libs.ktor.server.core)
                api(libs.ktor.server.cio)
                implementation(libs.bundles.kotlinx)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

        commonTest.dependencies {
            implementation(libs.ktor.server.test.host)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.immutable)
        }
    }
}
