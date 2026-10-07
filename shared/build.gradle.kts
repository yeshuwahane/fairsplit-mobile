import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.cybernerd404.fairsplit.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.firebase.messaging)
            implementation(libs.play.services.auth)
            implementation(libs.firebase.auth)
            // CameraX + ML Kit for QR scanning
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.camerax.core)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
        }
        commonMain.dependencies {
            val voyagerVersion = "1.1.0-beta02"

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)

            implementation(libs.napier)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Ktor 3.0.3 Networking Foundation
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Coil 3 Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // QR Code generation (pure KMP)
            implementation(libs.qrcode.kotlin)

            implementation("cafe.adriel.voyager:voyager-navigator:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-screenmodel:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-tab-navigator:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-transitions:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-koin:${voyagerVersion}")
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.configureEach {
    if (name == "syncComposeResourcesForIos") {
        enabled = false
    }
}