package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.mapper.toDataStore
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Repository managing streaming endpoint configuration.
 */
class EndpointRepository @Inject constructor(
    private val source: DataStoreSource,
) {
    /** Flow of current endpoint type */
    val type = source.store.map {
        it.endpointType.fromDataStore()
    }.distinctUntilChanged()

    /** Flow of OBS-specific endpoint configuration */
    val obsConfig = source.store.map {
        it.obsEndpoint.fromDataStore()
    }.distinctUntilChanged()

    /**
     * Flow of the configuration for the currently selected endpoint.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val endpointConfig: Flow<EndpointConfig> = type.flatMapLatest {
        when (it) {
            EndpointType.OBS -> obsConfig
        }
    }

    /**
     * Updates OBS endpoint configuration.
     *
     * @param data New OBS endpoint configuration
     * @return Result of the preference update
     */
    suspend fun setObsData(data: ObsConfig) = source.tryEdit {
        this.obsEndpoint = data.toDataStore()
    }
}
