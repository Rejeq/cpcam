plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.feature.about"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.common)

    implementation(libs.decompose.core)

    implementation(libs.androidx.core.ktx)

    implementation(libs.about.libraries.compose.m3)
}
