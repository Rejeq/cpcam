package com.rejeq.cpcam.core.endpoint

import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind
import com.rejeq.cpcam.core.endpoint.obs.StreamErrorKind

sealed interface EndpointResult {
    object Success : EndpointResult

    class Error(val kind: EndpointErrorKind) : EndpointResult
}

sealed interface EndpointErrorKind {
    object EndpointNotConfigured : EndpointErrorKind

    class ObsError(val kind: ObsErrorKind) : EndpointErrorKind
    class StreamError(val kind: StreamErrorKind) : EndpointErrorKind

    class UnknownError(val e: Exception) : EndpointErrorKind
}
