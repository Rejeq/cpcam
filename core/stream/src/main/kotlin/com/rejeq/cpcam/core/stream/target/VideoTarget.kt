package com.rejeq.cpcam.core.stream.target

import com.rejeq.cpcam.core.stream.relay.VideoRelay

interface VideoTarget {
    fun setRelay(relay: VideoRelay)

    fun start()
    fun stop()
}
