plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        filter {
            exclude("**/generated/**")
            exclude("**/*.gradle.kts/**")
        }
    verbose.set(true)
    outputToConsole.set(true)
    debug.set(true)
    }
}