// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false // Kotlin Parcelize
    alias(libs.plugins.kotlin.serialization) apply false // Kotlin Serialization
    alias(libs.plugins.gms.google.services) apply false // Google Services
    alias(libs.plugins.secrets.gradle.plugin) apply false // Secrets Gradle
}