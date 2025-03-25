package com.rejeq.cpcam.core.stream.relay

import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni

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

    override val surface: Surface get() = imageReader.surface

    init {
        stream.setResolution(width, height)

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let { frame ->
                image.format
                val buffers = image.planes.map { it.buffer }
                val strides = image.planes.map { it.rowStride }
                val pixelStrides = image.planes.map { it.pixelStride }

                stream.send(
                    image.timestamp,
                    image.width,
                    image.height,
                    image.format,
                    buffers.toTypedArray(),
                    strides.toIntArray(),
                    pixelStrides.toIntArray(),
                )

                frame.close()
            }
        }, bgHandler)
    }

    override fun start() {
        stream.start()
    }

    override fun stop() {
        stream.stop()
    }
}
