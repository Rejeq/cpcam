package com.rejeq.cpcam.core.endpoint

import kotlinx.coroutines.flow.Flow

/**
 * Represents the current state of a endpoint session.
 */
sealed interface EndpointState {
    /** Endpoint is not active */
    object Stopped : EndpointState

    /** Endpoint is establishing connection */
    object Connecting : EndpointState

    /** Endpoint is active and transmitting */
    class Started(val warning: EndpointResult) : EndpointState
}

interface Endpoint {
    val state: Flow<EndpointState>

    suspend fun connect()
    suspend fun disconnect()
}
