package com.rejeq.cpcam.core.stream.output

import com.github.michaelbull.result.Result
import com.rejeq.cpcam.core.stream.StreamErrorKind

internal interface StreamOutput {
    fun open(): Result<Unit, StreamErrorKind>
    fun close(): Result<Unit, StreamErrorKind>

    fun destroy()
}
