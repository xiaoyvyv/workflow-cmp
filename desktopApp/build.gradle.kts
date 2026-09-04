import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.compose.hot-reload")
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

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
