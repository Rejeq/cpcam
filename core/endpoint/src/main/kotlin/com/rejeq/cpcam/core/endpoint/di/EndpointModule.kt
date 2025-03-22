package com.rejeq.cpcam.core.endpoint.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WebsocketClient

@Module
@InstallIn(SingletonComponent::class)
internal object EndpointModule {
    @Provides
    @Singleton
    @WebsocketClient
    fun provideWebscoketClient(): Lazy<HttpClient> = lazy {
        HttpClient(CIO) {
            install(WebSockets) {
                pingIntervalMillis = 20_000
                contentConverter =
                    KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
}
