plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.feature.service"

    buildFeatures {
        androidResources = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.endpoint)
    implementation(projects.core.stream)
    implementation(projects.core.ui)

    implementation(libs.decompose.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.ktobs.core)
}
