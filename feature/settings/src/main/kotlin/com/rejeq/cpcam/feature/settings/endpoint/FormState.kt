package com.rejeq.cpcam.feature.settings.endpoint

sealed interface FormState<out T> {
    object Loading : FormState<Nothing>
    data class Success<T>(val data: T) : FormState<T>
}
