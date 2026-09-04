@file:Suppress("SpellCheckingInspection", "UnstableApiUsage")

plugins {
    id("workflow.library")

    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.graphics)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.resources)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            runtimeOnly(libs.compose.mediamp.runtime)
        }

        all {
            languageSettings {
                optIn("kotlin.io.encoding.ExperimentalEncodingApi")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
                optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.paging.ExperimentalPagingApi")
                optIn("org.orbitmvi.orbit.annotation.OrbitExperimental")
                optIn("androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi")
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

compose.resources {
    publicResClass = false
    generateResClass = never
}
