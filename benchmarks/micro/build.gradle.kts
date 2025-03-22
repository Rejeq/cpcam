plugins {
    id("cpcam.common")
    id("cpcam.android.lib")

    alias(libs.plugins.benchmark)
}

android {
    namespace = "com.rejeq.cpcam.microbenchmarks"

    defaultConfig {
        testInstrumentationRunner =
            "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }

    testBuildType = "release"
    buildTypes {
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        release {
            isDefault = true
        }
    }

    externalNativeBuild.cmake.path =
        file("./src/androidTest/cpp/CMakeLists.txt")
}

dependencies {
    androidTestImplementation(projects.core.data)

    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.benchmark.junit4)
}
