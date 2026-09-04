@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://jogamp.org/deployment/maven")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "workflow-cmp"

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":workflow-core")
include(":workflow-platform-ui")
include(":workflow-platform")
include(":workflow-node-base")
include(":workflow-node-control")
include(":workflow-node-data")
include(":workflow-node-codec")
include(":workflow-node-html")
include(":workflow-node-crypto")
include(":workflow-node-io")
include(":workflow-node-bilibili")
include(":workflow-node-all")
include(":workflow-node-testkit")
include(":workflow-ui-core")
include(":workflow-ui-image")
include(":workflow-ui-video")
include(":workflow-ui-webview")
include(":workflow-ui-all")
