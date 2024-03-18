import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.bundles.ktor)
            implementation(libs.bundles.kotlin)
            implementation(libs.bundles.multiplatformSettings)
            implementation(libs.bundles.voyager)
            implementation(libs.moko.permissions)

            implementation(libs.bundles.koin)
            implementation(libs.stately)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.bundles.androidx)
            implementation(libs.androidx.startup)
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.playservices.location)
            implementation(libs.kotlin.coroutines.playservices)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }
    }
}

compose {
    kotlinCompilerPlugin = libs.versions.jetbrains.compose.compiler.plugin.get()
}

android {
    namespace = "me.evko.forekast"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "me.evko.forekast"

        versionCode = 1
        versionName = "1.0"

        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.jetpack.compose.compiler.plugin.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

buildConfig {
    packageName("me.evko.forekast")

    buildConfigField("String", "API_KEY", getLocalProperty("apiKey"))
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
}
