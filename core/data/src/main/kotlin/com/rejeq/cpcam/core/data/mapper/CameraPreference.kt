package com.rejeq.cpcam.core.data.mapper

import android.util.Log
import android.util.Range
import com.rejeq.cpcam.core.data.model.CameraPreference
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.data.datastore.CameraPreferenceProto

/**
 * Mapping functions for camera preferences between domain and DataStore models.
 */

fun CameraPreferenceProto.fromDataStore() = this.let {
    val size = if (it.hasResolution()) {
        val out = Resolution.fromString(it.resolution)
        if (out == null) {
            Log.w(TAG, "Unable to parse resolution '${it.resolution}'")
        }

        out
    } else {
        null
    }

    val framerate = if (it.hasFramerate()) it.framerate else null

    CameraPreference(
        resolution = size,
        framerate = framerate?.parseToRange(),
    )
}

fun String.parseToRange(): Range<Int>? {
    val regex = """\s*(\d+)\s*,\s*(\d+)\s*""".toRegex()
    val matchResult = regex.matchEntire(this)

    return matchResult?.destructured?.let { (start, end) ->
        Range(start.toInt(), end.toInt())
    }
}

fun CameraPreference.toDataStore() = CameraPreferenceProto.newBuilder().let {
    if (resolution != null) {
        it.setResolution(resolution.toString())
    } else {
        it.clearResolution()
    }

    if (framerate != null) {
        it.setFramerate(framerate.toString())
    } else {
        it.clearFramerate()
    }

    it.build()
}

fun Map<String, CameraPreferenceProto>.fromDataStore() = mapValues {
    it.value.fromDataStore()
}

fun Map<String, CameraPreference>.toDataStore() = mapValues {
    it.value.toDataStore()
}

private const val TAG = "CameraPreferenceMapper"
