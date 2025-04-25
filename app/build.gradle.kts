plugins {
    id("cpcam.common")
    id("cpcam.android.app")
    id("cpcam.compose")

    id("kotlin-parcelize")
    alias(libs.plugins.android.application)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.about.libraries)
}

android {
    defaultConfig {
        applicationId = "com.rejeq.cpcam"
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        debug {
            packaging {
                jniLibs.keepDebugSymbols += listOf("**/*.so")
            }
        }
    }
}

aboutLibraries {
    collect {
        configPath = file("${rootProject.projectDir}/config")
    }
}

dependencies {
    implementation(projects.core.camera)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.endpoint)
    implementation(projects.core.ui)

    implementation(projects.feature.main)
    implementation(projects.feature.settings)
    implementation(projects.feature.about)
    implementation(projects.feature.service)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel)

    implementation(libs.decompose.core)
    implementation(libs.decompose.compose)
    implementation(libs.essenty.lifecycle.coroutines)

    implementation(libs.camerax.compose)

    implementation(libs.kotlinx.collections.immutables)

    implementation(libs.androidx.profileinstaller)
//    "baselineProfile"(project(":benchmarks"))

    debugImplementation(libs.leakcanary)
}
