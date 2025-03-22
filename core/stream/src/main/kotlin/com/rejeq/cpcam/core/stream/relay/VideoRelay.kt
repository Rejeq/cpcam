package com.rejeq.cpcam.core.stream.relay

import android.view.Surface

interface VideoRelay {
    val surface: Surface

    fun setResolution(width: Int, height: Int)

    fun start()
    fun stop()
}
