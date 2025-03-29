package com.rejeq.cpcam.core.endpoint

import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind

sealed interface EndpointResult {
    object Success : EndpointResult

    class ObsError(val kind: ObsErrorKind) : EndpointResult

    class UnknownError(val e: Exception) : EndpointResult
}
