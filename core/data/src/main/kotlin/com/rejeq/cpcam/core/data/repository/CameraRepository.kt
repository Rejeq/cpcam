package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.source.DataStoreSource
import com.rejeq.cpcam.data.datastore.CameraPreferenceProto
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Repository managing camera-specific preferences like resolution and
 * framerate.
 */
class CameraRepository @Inject constructor(
    private val source: DataStoreSource,
) {
    /** Flow of all camera preferences */
    val preferences get() = source.store.map {
        it.cameraPreferencesMap.fromDataStore()
    }.distinctUntilChanged()

    /**
     * Gets preferences for a specific camera.
     *
     * @param camera Camera identifier
     * @return Flow of camera preferences or null if not set
     */
    fun getPrefernce(camera: String) = source.store.map {
        it.cameraPreferencesMap[camera]?.fromDataStore()
    }.distinctUntilChanged()

    /**
     * Gets resolution preference for a specific camera.
     *
     * @param camera Camera identifier
     * @return Flow of resolution preference or null if not set
     */
    fun getResolution(camera: String) = source.store.map {
        it.cameraPreferencesMap[camera]?.fromDataStore()?.resolution
    }.distinctUntilChanged()

    /**
     * Updates resolution preference for a specific camera.
     *
     * @param camera Camera identifier
     * @param resolution New resolution to set, or null to clear
     * @return Result of the preference update
     */
    suspend fun setResolution(camera: String, resolution: Resolution?) =
        source.tryEdit {
            val builder = this.cameraPreferences[camera]?.toBuilder()
                ?: CameraPreferenceProto.newBuilder()

            builder.setResolution(resolution.toString())

            cameraPreferences[camera] = builder.build()
        }

    /**
     * Gets framerate preference for a specific camera.
     *
     * @param camera Camera identifier
     * @return Flow of framerate preference or null if not set
     */
    fun getFramerate(camera: String) = source.store.map {
        it.cameraPreferencesMap[camera]?.fromDataStore()?.framerate
    }.distinctUntilChanged()
}
