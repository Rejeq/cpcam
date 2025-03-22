package com.rejeq.cpcam.core.endpoint.obs

import com.rejeq.ktobs.AuthError
import com.rejeq.ktobs.ObsRequestException

sealed interface ObsErrorKind {
    class AuthFailed(val kind: AuthError) : ObsErrorKind

    class UnknownInput(val kind: InputKind) : ObsErrorKind
    class RequestFailed(val e: ObsRequestException) : ObsErrorKind
}
