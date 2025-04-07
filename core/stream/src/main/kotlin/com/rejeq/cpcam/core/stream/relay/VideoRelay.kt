package com.rejeq.cpcam.core.stream.relay

import android.view.Surface

/**
 * Interface for relaying raw video frames to an underlying video encoder.
 *
 * This interface defines the contract for components that forward raw,
 * unencoded video data to a video encoder. Implementations of this interface
 * act as a bridge between a video source (e.g., camera, screen capture) and an
 * encoder. The encoder uses the provided `Surface` to receive the raw frames
 * and process them for encoding.
 */
interface VideoRelay {
    val surface: Surface

    fun start()
    fun stop()
}
