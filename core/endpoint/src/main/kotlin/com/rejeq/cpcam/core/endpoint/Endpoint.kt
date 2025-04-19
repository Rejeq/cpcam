package com.rejeq.cpcam.core.endpoint

import com.rejeq.cpcam.core.data.model.EndpointConfig
import kotlinx.coroutines.flow.Flow

/**
 * Represents the current state of a endpoint session.
 */
sealed interface EndpointState {
    /** Endpoint is not active */
    data class Stopped(val reason: EndpointErrorKind? = null) : EndpointState

    /** Endpoint is establishing connection */
    data object Connecting : EndpointState

    /** Endpoint is active and transmitting */
    data class Started(val warning: EndpointErrorKind? = null) : EndpointState
}

interface Endpoint {
    val config: EndpointConfig

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
