plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
