// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies{
        classpath (libs.hilt.android.gradle.plugin)
        classpath (libs.kotlinx.serialization.json)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply true
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}