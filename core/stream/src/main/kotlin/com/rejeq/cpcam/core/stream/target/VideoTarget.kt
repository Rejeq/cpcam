package com.rejeq.cpcam.core.stream.target

import android.util.Range
import com.rejeq.cpcam.core.stream.relay.VideoRelay

interface VideoTarget {
    fun setRelay(relay: VideoRelay)

    fun setFramerate(framerate: Range<Int>)

    fun start()
    fun stop()
}
