plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.workflowEditorBridgeContract)
    implementation(projects.workflowEditorBridgeJvm)
    implementation(projects.workflowEditorMcp)
    testImplementation(projects.workflowCore)
    testImplementation(libs.kotlinx.immutable)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
