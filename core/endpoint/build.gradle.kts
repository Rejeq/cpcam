plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.endpoint"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.stream)

    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.json)

    implementation(libs.ktobs.core)
    implementation(libs.ktobs.ktor)
}
