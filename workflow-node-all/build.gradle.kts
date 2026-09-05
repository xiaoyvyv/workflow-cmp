plugins {
    id("workflow.library")
}

kotlin {
    android {
        namespace = "com.xiaoyv.workflow.node.all"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.workflowCore)
            implementation(projects.workflowPlatform)
            implementation(projects.workflowNodeBase)
            implementation(projects.workflowNodeControl)
            implementation(projects.workflowNodeData)
            implementation(projects.workflowNodeCodec)
            implementation(projects.workflowNodeHtml)
            implementation(projects.workflowNodeCrypto)
            implementation(projects.workflowNodeIo)
            implementation(projects.workflowNodeBilibili)
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.kmp.zip)
        }
    }
}

tasks.register<JavaExec>("exportManifest") {
    group = "workflow"
    description = "Exports default ActionEditorManifest JSON to workflow-editor-web/manifest.json"
    val jvmTarget = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    val compilation = jvmTarget.compilations.getByName("main")
    mainClass.set("com.xiaoyv.workflow.node.all.ManifestExportKt")
    classpath(compilation.output.allOutputs, compilation.runtimeDependencyFiles)

    val outputFile = layout.settingsDirectory.file("workflow-editor-web/manifest.json")
    outputs.file(outputFile)
    args(outputFile.asFile.absolutePath)
}
