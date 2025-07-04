plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.feature.scanner"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.camera)
    implementation(projects.core.cameraUi)
    implementation(projects.core.ui)
    implementation(projects.core.common)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.decompose.core)
    implementation(libs.decompose.compose)
    implementation(libs.essenty.lifecycle.coroutines)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
