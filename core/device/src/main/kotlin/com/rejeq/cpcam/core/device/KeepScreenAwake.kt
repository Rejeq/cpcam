package com.rejeq.cpcam.core.device

import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

fun isScreenKeepAwake(window: Window): Boolean =
    (window.attributes.flags and FLAG_KEEP_SCREEN_ON) != 0

fun keepScreenAwake(window: Window, keep: Boolean) {
    if (keep) {
        window.addFlags(FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(FLAG_KEEP_SCREEN_ON)
    }
}
