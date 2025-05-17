package com.rejeq.cpcam.core.stream.jni

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

internal class FFmpegOutputJni(protocol: String, host: String) {
    private val handle: Long = create(host, protocol)

    fun open(): Result<Unit, StreamError> {
        val res = open(handle)

        return if (res >= 0) {
            Ok(Unit)
        } else {
            Err(StreamError.fromCode(res) ?: StreamError.Unknown)
        }
    }

    fun close(): Result<Unit, StreamError> {
        val res = close(handle)

        return if (res >= 0) {
            Ok(Unit)
        } else {
            Err(StreamError.fromCode(res) ?: StreamError.Unknown)
        }
    }

    fun destroy() = destroy(handle)

    fun makeVideoStream(
        config: FFmpegVideoConfig,
    ): Result<FFmpegVideoStreamJni, StreamError> {
        val res = makeVideoStream(handle, config)
        return if (res > 0) {
            Ok(FFmpegVideoStreamJni(res))
        } else {
            Err(StreamError.fromCode(res.toInt()) ?: StreamError.Unknown)
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

    companion object {
        init {
            System.loadLibrary("cpcam_jni")
        }

        fun getSupportedFormats(codec: String): IntArray =
            nGetSupportedFormats(codec)
    }
}

private external fun nGetSupportedFormats(codec: String): IntArray
