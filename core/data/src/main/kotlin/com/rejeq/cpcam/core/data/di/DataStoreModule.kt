package com.rejeq.cpcam.core.data.di

import android.content.Context
import com.rejeq.cpcam.core.data.source.DataStoreSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {
    @Provides
    @Singleton
    fun providesDataStoreSource(@ApplicationContext context: Context) =
        DataStoreSource(
            scope = CoroutineScope(Dispatchers.Unconfined),
            produceFile = {
                File(context.filesDir, "datastore.preferences_pb")
            },
        )
}
