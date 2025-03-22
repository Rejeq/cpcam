package com.rejeq.cpcam.core.data.model

/**
 * Types of supported streaming endpoints.
 */
enum class EndpointType {
    /** OBS Studio streaming endpoint */
    OBS,
}

sealed interface EndpointConfig

/**
 * Configuration data for OBS Studio endpoint.
 *
 * @property url WebSocket URL of the OBS instance
 * @property port WebSocket port number
 * @property password Authentication password, empty if not used
 */
data class ObsConfig(val url: String, val port: Int, val password: String) :
    EndpointConfig
