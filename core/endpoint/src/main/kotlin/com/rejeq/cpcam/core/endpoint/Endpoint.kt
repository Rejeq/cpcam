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
    /**
     * Current state of the endpoint.
     */
    val state: Flow<EndpointState>

    /**
     * Establishes a connection to the remote endpoint.
     *
     * @return New state of the endpoint after this operation.
     */
    suspend fun connect(): EndpointState

    /**
     * Disconnects from the endpoint.
     *
     * @return New state of the endpoint after this operation
     */
    suspend fun disconnect(): EndpointState
}
