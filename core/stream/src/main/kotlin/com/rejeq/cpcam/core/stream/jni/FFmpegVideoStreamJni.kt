package com.rejeq.cpcam.core.stream.jni

import java.nio.ByteBuffer

internal class FFmpegVideoStreamJni(val handle: Long) {
    companion object {
        init {
            System.loadLibrary("cpcam_jni")
        }
    }

    fun send(
        ts: Long,
        width: Int,
        height: Int,
        format: Int,
        planeCount: Int,
        buffers: Array<ByteBuffer?>,
        strides: IntArray,
        pixelStrides: IntArray,
    ) = send(
        handle,
        ts,
        width,
        height,
        format,
        planeCount,
        buffers,
        strides,
        pixelStrides,
    )

    fun setResolution(width: Int, height: Int) =
        setResolution(handle, width, height)

    fun start() = start(handle)

    fun stop() = stop(handle)

    private external fun send(
        handle: Long,
        ts: Long,
        width: Int,
        height: Int,
        format: Int,
        planeCount: Int,
        buffers: Array<ByteBuffer?>,
        strides: IntArray,
        pixelStrides: IntArray,
    )

    private external fun setResolution(handle: Long, width: Int, height: Int)

    private external fun start(handle: Long)
    private external fun stop(handle: Long)
}
