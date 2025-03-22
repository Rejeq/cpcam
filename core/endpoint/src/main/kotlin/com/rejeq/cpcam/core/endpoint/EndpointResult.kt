package com.rejeq.cpcam.core.endpoint

import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind

sealed interface EndpointResult<out T> {
    class Success<out T>(val value: T) : EndpointResult<T>

    class UnknownHost : EndpointResult<Nothing>
    class ConnectionRefused : EndpointResult<Nothing>
    class ConnectionTimeout : EndpointResult<Nothing>

    class ObsError(val kind: ObsErrorKind) : EndpointResult<Nothing>

    class UnknownError(val e: Exception) : EndpointResult<Nothing>
}
