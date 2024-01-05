import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
}

kotlin {
    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    cocoapods {
        version = "1.0.0"
        summary = "shared"
        homepage = "https://github.com/evilya/forekast"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val ktorVersion = "2.3.5"
        val kotlinSerializationVersion = "1.6.0"
        val coroutinesVersion = "1.7.3"
        val settingsVersion = "1.1.1"

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("com.russhwolf:multiplatform-settings-no-arg:$settingsVersion")
            implementation("com.russhwolf:multiplatform-settings-serialization:$settingsVersion")
            implementation("com.russhwolf:multiplatform-settings-coroutines:$settingsVersion")
            implementation("dev.icerock.moko:permissions-compose:0.17.0")
        }

        androidMain.dependencies {
            api("androidx.activity:activity-compose:1.8.0")
            api("androidx.appcompat:appcompat:1.6.1")
            api("androidx.core:core-ktx:1.12.0")
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            implementation("androidx.startup:startup-runtime:1.1.1")
            implementation("com.google.android.gms:play-services-location:21.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
        }

        iosMain.dependencies {
            api("io.ktor:ktor-client-darwin:$ktorVersion")
        }
    }
}

compose {
    kotlinCompilerPlugin = "1.5.4-dev1-kt2.0.0-Beta1"
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "me.evko.forekast.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

buildkonfig {
    packageName = "me.evko.forekast"

    defaultConfigs {
        buildConfigField(STRING, "API_KEY", getLocalProperty("apiKey"))
    }
}