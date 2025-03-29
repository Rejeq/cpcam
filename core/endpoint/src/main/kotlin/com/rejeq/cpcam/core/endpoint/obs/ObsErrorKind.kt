package com.rejeq.cpcam.core.endpoint.obs

import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.ktobs.AuthError
import com.rejeq.ktobs.ObsRequestException

sealed interface ObsErrorKind {
    object NotHaveData : ObsErrorKind

    object UnknownHost : ObsErrorKind
    object ConnectionRefused : ObsErrorKind
    object ConnectionTimeout : ObsErrorKind

    class AuthFailed(val kind: AuthError) : ObsErrorKind

    class UnknownInput(val kind: InputKind) : ObsErrorKind
    class RequestFailed(val e: ObsRequestException) : ObsErrorKind

    class Unknown(val e: Exception) : ObsErrorKind
}

fun ObsErrorKind.toEndpointError() = when (this) {
    is ObsErrorKind.Unknown -> EndpointErrorKind.UnknownError(this.e)
    else -> EndpointErrorKind.ObsError(this)
}
