package com.rejeq.cpcam.core.stream.target

import com.rejeq.cpcam.core.audio.target.RecordAudioTarget
import com.rejeq.cpcam.core.data.model.SampleFormat
import com.rejeq.cpcam.core.stream.relay.FFmpegAudioRelay
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class AudioTarget @Inject constructor(private val target: RecordAudioTarget) {
    private var relay: FFmpegAudioRelay? = null

    // FIXME:
//    val audioBuffer: ByteBuffer = ByteBuffer.allocateDirect(target.bufferSize)
//    val audioBuffer: ByteBuffer = ByteBuffer.allocateDirect(1024)
    val audioBuffer: ByteBuffer = ByteBuffer.allocateDirect(14112)

    private var recordingThread: Thread? = null

    fun setRelay(relay: FFmpegAudioRelay) {
        this.relay = relay
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun start() {
        target.start()
        relay?.start()

        recordingThread = Thread {
            while (target.isRecording()) {
                val data = target.read(audioBuffer)
                relay?.send(
                    audioBuffer,
                    data.timestampNs,
                    data.sizeInBytes,
                    44100,
                    1,
//                    SampleFormat.PCM_S16LE,
                    SampleFormat.PCM_F32LE,
                )
            }
        }

        recordingThread?.start()
    }

    fun stop() {
        recordingThread = null

        target.stop()
        relay?.stop()
    }
}
