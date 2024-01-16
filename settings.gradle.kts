rootProject.name = "Forekast"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
    }
}

include(":androidApp")
include(":shared")
include(":desktopApp")