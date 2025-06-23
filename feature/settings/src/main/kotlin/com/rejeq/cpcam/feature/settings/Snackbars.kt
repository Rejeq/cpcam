package com.rejeq.cpcam.feature.settings

import androidx.compose.material3.SnackbarDuration
import com.rejeq.cpcam.core.ui.SnackbarState

fun prefFailWriteSnackbar(action: suspend () -> Unit) = SnackbarState(
    messageResId = R.string.pref_snackbar_fail_write_message,
    actionResId = R.string.pref_snackbar_fail_write_action,
    duration = SnackbarDuration.Short,
    withDismissAction = true,
    action = action,
)

fun prefTooManyErrorsSnackbar(action: () -> Unit) = SnackbarState(
    messageResId = R.string.pref_snackbar_too_many_errors_message,
    actionResId = R.string.pref_snackbar_too_many_errors_action,
    duration = SnackbarDuration.Short,
    withDismissAction = true,
    action = action,
)
