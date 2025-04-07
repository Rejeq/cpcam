plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.device"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
}
