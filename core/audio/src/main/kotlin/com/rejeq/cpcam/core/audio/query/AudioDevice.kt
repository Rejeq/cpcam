package com.rejeq.cpcam.core.audio.query

import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

fun queryDevices(manager: AudioManager): Array<AudioDeviceInfo> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return manager.getDevices(AudioManager.GET_DEVICES_INPUTS)
    } else {
        // TODO: Implement for older versions.
        // it can be a bit tricky, since there no convenient API.
        //
        // To get list of connected devices you can manually
        // check isWiredHeadsetOn() and isSpeakerphoneOn() methods (see for
        // details: https://stackoverflow.com/questions/34945313/what-is-the-android-api-for-getting-the-list-of-connected-audio-devices).
        // Also I think CAMCORDER and MIC must be also supported on every phone
        // that has camera
        //
        // Also these configurations guaranteed to be supported on all phones:
        // format = ENCODING_PCM_16BIT: https://developer.android.com/reference/android/media/AudioFormat#ENCODING_PCM_16BIT
        // sampleRate = 44100Hz: https://developer.android.com/reference/android/media/AudioRecord#AudioRecord(int,%20int,%20int,%20int,%20int)
        // channels = AudioFormat.CHANNEL_IN_MONO: https://developer.android.com/reference/android/media/AudioRecord#AudioRecord(int,%20int,%20int,%20int,%20int)
        return emptyArray()
    }
}
