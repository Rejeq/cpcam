object ProjectConfig {
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
    const val MIN_SDK = 21

    const val JVM_TARGET = "17"
    const val JVM_TOOLCHAIN = 17

    // FIXME: Sync this property with ndk version of jni dependencies
    const val NDK_VERSION = "28.0.13004108"
    val SUPPORTED_ABI = listOf("x86_64", "arm64-v8a", "armeabi-v7a")

    const val NAMESPACE = "com.rejeq.cpcam"
}
