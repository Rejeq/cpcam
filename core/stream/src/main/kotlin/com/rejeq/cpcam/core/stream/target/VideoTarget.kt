package com.rejeq.cpcam.core.stream.target

import android.util.Range
import com.rejeq.cpcam.core.stream.relay.Relay
import com.rejeq.cpcam.core.stream.relay.VideoRelay

interface Target<in State : TargetState> {
    suspend fun use(state: State, block: suspend () -> Unit)
}

/**
 * Responsible for fetching video data from a specific source
 * and relaying it to a [VideoRelay].
 */
interface VideoTarget : Target<VideoTargetState> {
    override suspend fun use(state: VideoTargetState, block: suspend () -> Unit)
}

interface TargetState {
    val enabled: Boolean
    val relay: Relay
}

data class VideoTargetState(
    override val enabled: Boolean,
    override val relay: VideoRelay,
    val framerate: Range<Int>?,
) : TargetState
