package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
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
        it.dimScreenDelay.toDuration(DurationUnit.MILLISECONDS)
    }.distinctUntilChanged()

    suspend fun setDimScreenDelay(delay: Duration) = source.tryEdit {
        this.dimScreenDelay = delay.inWholeMilliseconds
    }
}
