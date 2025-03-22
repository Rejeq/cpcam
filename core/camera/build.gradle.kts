plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.camera"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)
}
