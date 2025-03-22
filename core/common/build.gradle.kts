plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.collections.immutables)
}
