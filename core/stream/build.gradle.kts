plugins {
    id("cpcam.common")
    id("cpcam.android.lib")
}

android {
    namespace = "com.rejeq.cpcam.core.stream"

    externalNativeBuild.cmake.path = file("./src/main/cpp/CMakeLists.txt")

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")

        externalNativeBuild.cmake {
            val depsDir = System.getenv("CPCAM_DEPS_TARGET_DIR")
                ?: "${project.rootDir}/jni_deps/targets"

            arguments += listOf(
                "-DCMAKE_EXPORT_COMPILE_COMMANDS=1",
                "-DDEPS_TARGET_DIR='$depsDir'",
            )
        }
    }
}

dependencies {
    implementation(projects.core.audio)
    implementation(projects.core.camera)
    implementation(projects.core.common)
    implementation(projects.core.data)

    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.coroutines)
}
