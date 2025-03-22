plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.endpoint"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.stream)
    implementation(projects.core.common)
    // TODO: Remove this dependency, it is required for notification,
    //  to get resources
    implementation(projects.core.ui)

    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.json)

    implementation(libs.ktobs.core)
    implementation(libs.ktobs.ktor)
}
