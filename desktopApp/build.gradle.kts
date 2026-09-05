import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.compose.hot-reload")
}

dependencies {
    implementation(projects.shared)
    implementation(projects.workflowCore)
    implementation(projects.workflowEditorBridge)
    implementation(projects.workflowNodeAll)

    implementation(compose.desktop.currentOs)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)

    implementation(libs.compose.ui.tooling.preview)
}


compose.desktop {
    application {
        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        mainClass = "com.xiaoyv.workflow.MainKt"

        nativeDistributions {
            modules("java.base", "java.desktop", "java.sql", "java.logging", "java.naming", "java.prefs", "java.management", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.xiaoyv.workflow"
            packageVersion = "1.0.0"
        }
    }
}
