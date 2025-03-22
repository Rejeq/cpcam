plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
    id("cpcam.compose")
}

android {
    namespace = "com.rejeq.cpcam.core.ui"
}

dependencies {
    implementation(projects.core.data)

    implementation(libs.androidx.core.ktx)

    implementation(libs.decompose.core)
    implementation(libs.decompose.compose)

    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.animation.graphics)
}
