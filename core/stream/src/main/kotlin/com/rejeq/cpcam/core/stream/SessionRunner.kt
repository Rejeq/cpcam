package com.rejeq.cpcam.core.stream

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.output.StreamOutput
import com.rejeq.cpcam.core.stream.target.TargetState
import com.rejeq.cpcam.core.stream.target.VideoTargetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SessionRunner internal constructor(
    private val output: StreamOutput,
    streams: List<Stream<*>>,
) {
    private val streams = streams.toMutableList()

    private var scope: CoroutineScope? = null
    private val streamJobs = mutableMapOf<Stream<*>, Job?>()

    suspend fun use(block: suspend () -> Unit): Result<Unit, StreamErrorKind> {
        if (scope != null) {
            return Err(StreamErrorKind.AlreadyStarted)
        }

        assert(streamJobs.values.all { it == null }) {
            "All streams must be stopped"
        }

        return launchOutput {
            coroutineScope {
                try {
                    scope = this
                    streams.forEach { stream ->
                        if (stream.state.enabled) {
                            launchStream(stream)
                        }
                    }

                    try {
                        block()
                        Ok(Unit)
                    } finally {
                        for ((stream, job) in streamJobs) {
                            job?.cancel()
                            streamJobs[stream] = null
                        }
                    }
                } finally {
                    scope = null
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateVideoState(newState: VideoTargetState) {
        for ((idx, stream) in streams.withIndex()) {
            if (stream.state is VideoTargetState) {
                val stream = stream as Stream<VideoTargetState>
                val newStream = updateStreamState(stream, newState)
                streams[idx] = newStream
            }
        }
    }

    fun destroy() {
        scope?.cancel()

        for (stream in streams) {
            stream.state.relay.destroy()
        }

        output.destroy()
    }

    private suspend fun launchOutput(
        block: suspend () -> Unit,
    ): Result<Unit, StreamErrorKind> = try {
        output.open()
            .onFailure { return Err(it) }

        block()
        Ok(Unit)
    } finally {
        output.close()
    }

    private fun <T : TargetState> CoroutineScope.launchStream(
        stream: Stream<T>,
    ) {
        streamJobs[stream]?.cancel()
        streamJobs[stream] = launch {
            stream.target.use(stream.state) {
                awaitCancellation()
            }
        }
    }

    private fun <T : TargetState> updateStreamState(
        stream: Stream<T>,
        newState: T,
    ): Stream<T> {
        var wasStarted = false
        if (streamJobs[stream] != null) {
            wasStarted = true
        }

        val newStream = stream.copy(state = newState)

        val scope = this.scope
        if (wasStarted && scope != null) {
            scope.launchStream(newStream)
        }

        return newStream
    }

    companion object {
        fun getSupportedCodecs(): List<VideoCodec> =
            FFmpegOutput.getSupportedCodecs()

        fun getSupportedFormats(codec: VideoCodec): List<PixFmt> =
            FFmpegOutput.getSupportedFormats(codec)
    }
}
