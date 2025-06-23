package com.rejeq.cpcam.ui

import androidx.compose.material3.SnackbarDuration
import com.rejeq.cpcam.R
import com.rejeq.cpcam.core.data.source.DataSourceError
import com.rejeq.cpcam.core.ui.SnackbarState

fun DataSourceError.toSnackbarState() = SnackbarState(
    messageResId = R.string.snackbar_data_source_error,
    duration = SnackbarDuration.Indefinite,
    withDismissAction = true,
)
