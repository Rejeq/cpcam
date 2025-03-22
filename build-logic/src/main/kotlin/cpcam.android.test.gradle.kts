plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = ProjectConfig.COMPILE_SDK

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = ProjectConfig.JVM_TARGET
    }

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK
        targetSdk = ProjectConfig.TARGET_SDK
    }
}
