package com.rejeq.cpcam.microbenchmarks

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.mapper.toDataStore
import com.rejeq.cpcam.core.data.model.CameraPreference
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.data.datastore.AppPreferences
import com.rejeq.cpcam.data.datastore.CameraPreferenceProto
import com.rejeq.cpcam.data.datastore.copy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    val pref = AppPreferences.newBuilder().build()
    val camera = "0"
    val resolution = Resolution(1920, 1080)

    @Test
    fun measureProtobufBuilder() = benchmarkRule.measureRepeated {
        pref.copy {
            val builder = cameraPreferences[camera]?.toBuilder()
                ?: CameraPreferenceProto.newBuilder()

            builder.setResolution(resolution.toString())

            cameraPreferences[camera] = builder.build()
        }
    }

    @Test
    fun measureProtobufCopyToDomain() = benchmarkRule.measureRepeated {
        pref.copy {
            val oldPref = cameraPreferences[camera]
                ?.fromDataStore() ?: CameraPreference()
            val newPref = oldPref.copy(resolution = resolution)

            cameraPreferences[camera] = newPref.toDataStore()
        }
    }
}
