package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.mapper.fromDataStore
import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class StreamRepository @Inject constructor(source: DataStoreSource) {
    val obsData = source.store.map {
        it.obsEndpoint.streamData.fromDataStore()
    }.distinctUntilChanged()
}
