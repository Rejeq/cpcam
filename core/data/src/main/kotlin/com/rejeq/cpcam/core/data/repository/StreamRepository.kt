package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.mapper.toDataStore
import com.rejeq.cpcam.core.data.model.AudioConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.source.DataStoreSource
import com.rejeq.cpcam.data.datastore.ObsEndpointProto
import com.rejeq.cpcam.data.datastore.ObsStreamDataProto
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class StreamRepository @Inject constructor(
    private val source: DataStoreSource,
) {
    val obsData = source.store.map {
        it.obsEndpoint.streamData.fromDataStore()
    }.distinctUntilChanged()

    suspend fun setObsData(data: ObsStreamData) = source.tryEdit {
        val builder = this.obsEndpoint.toBuilder()
            ?: ObsEndpointProto.newBuilder()

        builder.streamData = data.toDataStore()

        this.obsEndpoint = builder.build()
    }

    suspend fun setObsVideoConfig(config: VideoConfig) = source.tryEdit {
        val endBuilder = this.obsEndpoint.toBuilder()
            ?: ObsEndpointProto.newBuilder()

        val builder = this.obsEndpoint.streamData?.toBuilder()
            ?: ObsStreamDataProto.newBuilder()

        builder.videoConfig = config.toDataStore()
        endBuilder.streamData = builder.build()

        this.obsEndpoint = endBuilder.build()
    }

    suspend fun setObsAudioConfig(config: AudioConfig) = source.tryEdit {
        val endBuilder = this.obsEndpoint.toBuilder()
            ?: ObsEndpointProto.newBuilder()

        val builder = this.obsEndpoint.streamData?.toBuilder()
            ?: ObsStreamDataProto.newBuilder()

        builder.audioConfig = config.toDataStore()
        endBuilder.streamData = builder.build()

        this.obsEndpoint = endBuilder.build()
    }
}
