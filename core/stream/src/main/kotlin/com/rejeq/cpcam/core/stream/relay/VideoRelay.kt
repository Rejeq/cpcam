package com.rejeq.cpcam.core.stream.relay

import android.view.Surface

interface VideoRelay {
    val surface: Surface

    fun start()
    fun stop()
}
