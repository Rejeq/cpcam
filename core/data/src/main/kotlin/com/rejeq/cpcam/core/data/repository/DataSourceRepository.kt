package com.rejeq.cpcam.core.data.repository

import com.rejeq.cpcam.core.data.source.DataStoreSource
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.asSharedFlow

class DataSourceRepository @Inject constructor(source: DataStoreSource) {
    @OptIn(ExperimentalTime::class)
    val errors = source.errors.asSharedFlow()
}
