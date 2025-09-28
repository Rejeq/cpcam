package com.rejeq.cpcam.core.endpoint

import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind
import com.rejeq.cpcam.core.endpoint.obs.ObsStreamErrorKind

sealed interface EndpointErrorKind {
    object EndpointNotConfigured : EndpointErrorKind
    object FailedRetrieveEndpoint : EndpointErrorKind

    class ObsError(val kind: ObsErrorKind) : EndpointErrorKind
    class StreamError(val kind: ObsStreamErrorKind) : EndpointErrorKind

    class UnknownError(val e: Exception) : EndpointErrorKind
}
