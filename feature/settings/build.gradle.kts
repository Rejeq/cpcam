plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.feature.settings"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    implementation(projects.core.camera)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.device)
    implementation(projects.core.endpoint)
    implementation(projects.core.ui)

    implementation(libs.decompose.core)
    implementation(libs.essenty.lifecycle.coroutines)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.collections.immutables)
}
