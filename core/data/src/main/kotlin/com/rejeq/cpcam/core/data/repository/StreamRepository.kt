package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.mapper.toDataStore
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.source.DataStoreSource
import com.rejeq.cpcam.data.datastore.ObsEndpointProto
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
}
