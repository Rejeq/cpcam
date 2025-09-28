package com.rejeq.cpcam.core.endpoint

import com.github.michaelbull.result.Result
import com.rejeq.cpcam.core.data.model.EndpointConfig
import kotlinx.coroutines.flow.Flow

/**
 * Represents the current state of a endpoint session.
 */
sealed interface EndpointState {
    /** Endpoint is not active */
    data object Stopped : EndpointState

    /** Endpoint is establishing connection */
    data object Connecting : EndpointState

    /** Endpoint is active and transmitting */
    data class Started(val warning: EndpointErrorKind? = null) : EndpointState

    data class Failed(val reason: EndpointErrorKind) : EndpointState
}

interface Endpoint {
    val config: EndpointConfig

    /**
     * Current state of the endpoint.
     */
    val state: Flow<EndpointState>

    /**
     * Establishes a connection to the remote endpoint.
     */
    suspend fun connect(): Result<Unit, EndpointErrorKind>

    suspend fun disconnect()
}
