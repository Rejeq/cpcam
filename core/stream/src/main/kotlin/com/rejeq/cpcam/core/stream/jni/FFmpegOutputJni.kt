package com.rejeq.cpcam.core.stream.jni

import com.rejeq.cpcam.core.stream.StreamError

internal class FFmpegOutputJni(protocol: String, host: String) {
    companion object {
        init {
            System.loadLibrary("cpcam_jni")
        }
    }

    private val handle: Long = create(host, protocol)

    fun open(): StreamError? = StreamError.fromCode(open(handle))
    fun close(): StreamError? = StreamError.fromCode(close(handle))
    fun destroy() = destroy(handle)

    fun makeVideoStream(config: FFmpegVideoConfig): FFmpegVideoStreamJni? {
        val res = makeVideoStream(handle, config)

        return if (res > 0) {
            FFmpegVideoStreamJni(res)
        } else {
            // TODO: Return specific error
            // StreamError.fromCode(res)
            null
        }
    }

    private external fun create(host: String, protocol: String): Long
    external fun destroy(handle: Long)

    private external fun open(handle: Long): Int
    private external fun close(handle: Long): Int

    private external fun makeVideoStream(
        handle: Long,
        config: FFmpegVideoConfig,
    ): Long
}
