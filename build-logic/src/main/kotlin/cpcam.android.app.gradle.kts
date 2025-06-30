import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val keystoreProp = Properties()

// To allow building project when keystore.properties does not exist
val keystoreBaseFile = rootProject.file("keystore_base.properties")
keystoreProp.load(FileInputStream(keystoreBaseFile))

val keystoreFile = rootProject.file("keystore.properties")
if (keystoreFile.exists()) {
    keystoreProp.load(FileInputStream(keystoreFile))
}

android {
    namespace = ProjectConfig.NAMESPACE
    compileSdk = ProjectConfig.COMPILE_SDK
    ndkVersion = ProjectConfig.NDK_VERSION

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK
        targetSdk = ProjectConfig.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        //noinspection ChromeOsAbiSupport
        ndk.abiFilters += ProjectConfig.SUPPORTED_ABI
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProp["storeFile"] as String)
            storePassword = keystoreProp["storePassword"] as String
            keyAlias = keystoreProp["keyAlias"] as String
            keyPassword = keystoreProp["keyPassword"] as String
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"

            val hash = gitCommitHash()?.let { "-$it" }
            versionNameSuffix = hash ?: ""
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

        // HACK: Use some option in protobuf to disable generating this files
        resources.excludes += "google/protobuf/**"
        resources.excludes += "src/google/protobuf/**"
    }

    lint {
        textReport = true
        textOutput =
            layout.buildDirectory
                .file("reports/lint/results.txt")
                .getOrNull()
                ?.asFile

        htmlReport = true
        textOutput =
            layout.buildDirectory
                .file("reports/lint/results.html")
                .getOrNull()
                ?.asFile

        xmlReport = false
        sarifReport = false
    }

    androidResources.localeFilters += listOf("en", "ru")
    kotlinOptions.jvmTarget = ProjectConfig.JVM_TARGET
    buildFeatures.compose = true
    buildFeatures.buildConfig = true
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_TOOLCHAIN)

    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }
}
