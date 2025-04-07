package com.rejeq.cpcam.core.device

import android.view.Window

fun dimScreen(window: Window) {
    val attr = window.attributes
    attr.screenBrightness = MIN_SCREEN_BRIGHTNESS
    window.attributes = attr
}

fun restoreScreenBrightness(window: Window) {
    val attr = window.attributes
    attr.screenBrightness = -1f
    window.attributes = attr
}

private const val MIN_SCREEN_BRIGHTNESS = 0.0f
