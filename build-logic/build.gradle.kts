plugins {
    `kotlin-dsl`
}

repositories {
    maven { url = uri("https://maven.google.com") }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.gradle.versions.gradlePlugin)

    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)

    implementation(libs.kotlin.android.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.android.application.gradlePlugin)
    implementation(libs.android.library.gradlePlugin)
    implementation(libs.android.test.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)

    implementation(libs.detekt.gradlePlugin)
}
