package com.rejeq.cpcam.core.device

import android.content.ContentResolver
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
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

fun screenOffTimeoutMs(contentResolver: ContentResolver): Long? = try {
    Settings.System.getInt(
        contentResolver,
        Settings.System.SCREEN_OFF_TIMEOUT,
    ).toLong()
} catch (e: SettingNotFoundException) {
    Log.e(TAG, "Unable to get screen off timeout", e)
    null
}

private const val TAG = "DimScreen"
private const val MIN_SCREEN_BRIGHTNESS = 0.0f
