package com.rejeq.cpcam.core.stream.jni

internal class FFmpegOutputJni(protocol: String, host: String) {
    companion object {
        init {
            System.loadLibrary("cpcam_jni")
        }
    }

    private val handle: Long = create(host, protocol)

    fun open() = open(handle)
    fun close() = close(handle)

    fun makeVideoStream(config: FFmpegVideoConfig) =
        FFmpegVideoStreamJni(makeVideoStream(handle, config))

    private external fun create(host: String, protocol: String): Long
    external fun destroy(handle: Long)

    private external fun open(handle: Long): Boolean
    private external fun close(handle: Long): Boolean

    private external fun makeVideoStream(
        handle: Long,
        config: FFmpegVideoConfig,
    ): Long
}
