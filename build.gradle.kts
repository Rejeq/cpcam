plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.android.test).apply(false)

    alias(libs.plugins.baselineprofile).apply(false)
    alias(libs.plugins.benchmark).apply(false)

    alias(libs.plugins.protobuf).apply(false)

    id("cpcam.gradle.versions")
}
