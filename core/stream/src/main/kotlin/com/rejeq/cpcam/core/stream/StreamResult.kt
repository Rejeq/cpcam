package com.rejeq.cpcam.core.stream

sealed interface StreamResult<out T> {
    class Success<out T>(val value: T) : StreamResult<T>

    object Failed : StreamResult<Nothing>
}
