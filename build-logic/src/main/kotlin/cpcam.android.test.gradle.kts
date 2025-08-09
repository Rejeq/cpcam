plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = ProjectConfig.COMPILE_SDK

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK
        targetSdk = ProjectConfig.TARGET_SDK
    }
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_TOOLCHAIN)

    compilerOptions {
        jvmTarget = ProjectConfig.JVM_TARGET
    }
}
