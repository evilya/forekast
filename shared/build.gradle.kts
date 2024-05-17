import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
     alias(libs.plugins.buildConfig)    
}

buildConfig {
    packageName("me.evko.forekast")

    buildConfigField("String", "API_KEY", getLocalProperty("apiKey"))
}