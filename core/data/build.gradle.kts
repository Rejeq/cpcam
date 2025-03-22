plugins {
    id("cpcam.common")
    id("cpcam.android.lib")

    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.rejeq.cpcam.core.data"

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

protobuf {
    protoc {
        val protoc = libs.protobuf.protoc.get()
        artifact = protoc.toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }

                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    api(libs.androidx.dataStore)
    api(libs.protobuf.kotlin.lite)
}
