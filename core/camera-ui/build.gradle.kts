plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.core.camera.ui"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    implementation(projects.core.camera)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.device)
    implementation(projects.core.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.decompose.core)

    implementation(libs.camerax.core)
}
