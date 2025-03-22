package com.rejeq.cpcam.core.endpoint

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the current state of a endpoint session.
 */
enum class EndpointState {
    /** Endpoint is not active */
    Stopped,

    /** Endpoint is establishing connection */
    Connecting,

    /** Endpoint is active and transmitting */
    Started,
}

interface Endpoint {
    val state: StateFlow<EndpointState>

    suspend fun connect()
    suspend fun disconnect()
}
