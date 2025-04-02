plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    kotlin("plugin.serialization")
}

android {
    compileSdk = ProjectConfig.COMPILE_SDK
    ndkVersion = ProjectConfig.NDK_VERSION

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //noinspection ChromeOsAbiSupport
        ndk.abiFilters += ProjectConfig.SUPPORTED_ABI
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = ProjectConfig.JVM_TARGET
    }
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_TOOLCHAIN)
}
