package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ScreenRepository @Inject constructor(
    private val source: DataStoreSource,
) {
    val keepScreenAwake = source.store.map {
        it.keepScreenAwake
    }.distinctUntilChanged()

    suspend fun setKeepScreenAwake(keepScreenAwake: Boolean) = source.tryEdit {
        this.keepScreenAwake = keepScreenAwake
    }

    val dimScreenDelay = source.store.map {
        it.dimScreenDelay
    }.distinctUntilChanged()

    suspend fun setDimScreenDelay(ms: Long) = source.tryEdit {
        this.dimScreenDelay = ms
    }
}
