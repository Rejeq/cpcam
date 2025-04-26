package com.rejeq.cpcam.core.stream

import com.rejeq.cpcam.core.stream.jni.StreamError

sealed interface StreamErrorKind {
    data object NoVideoConfig : StreamErrorKind
    data object InvalidVideoStream : StreamErrorKind
    data class FFmpegError(val kind: StreamError) : StreamErrorKind
}
