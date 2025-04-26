package com.rejeq.cpcam.core.stream.output

import com.rejeq.cpcam.core.stream.StreamErrorKind

internal interface StreamOutput {
    fun open(): StreamErrorKind?
    fun close(): StreamErrorKind?

    fun destroy()
}
