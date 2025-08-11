package com.rejeq.cpcam.core.stream.relay

import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import java.nio.ByteBuffer

internal class FFmpegVideoRelay(
    private val stream: FFmpegVideoStreamJni,
    width: Int,
    height: Int,
    format: Int = ImageFormat.YUV_420_888,
    maxImages: Int = 2,
) : VideoRelay {
    private var bgThread = HandlerThread("FFmpegVideoEncoder").apply { start() }
    private var bgHandler = Handler(bgThread.looper)
    private val imageReader = ImageReader.newInstance(
        width,
        height,
        format,
        maxImages,
    )

    // NOTE: The length of arrays must be synced with jni FrameData
    private val buffers = arrayOfNulls<ByteBuffer>(4)
    private val strides = IntArray(4)
    private val pixelStrides = IntArray(4)

    override val surface: Surface get() = imageReader.surface

    init {
        stream.setResolution(width, height)

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val len = planes.size

                for (i in planes.indices) {
                    val plane = planes[i]

                    buffers[i] = plane.buffer
                    strides[i] = plane.rowStride
                    pixelStrides[i] = plane.pixelStride
                }

                stream.send(
                    image.timestamp,
                    image.width,
                    image.height,
                    image.format,
                    len,
                    buffers,
                    strides,
                    pixelStrides,
                )

                image.close()
            }
        }, bgHandler)
    }

    override fun start() {
        stream.start()
    }

    override fun stop() {
        stream.stop()
    }

    override fun destroy() {
        bgThread.quitSafely()
    }
}
