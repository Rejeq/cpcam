package com.rejeq.cpcam.feature.settings

import androidx.compose.material3.SnackbarDuration
import com.rejeq.cpcam.core.device.BatteryOptimizationError
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

fun BatteryOptimizationError.toSnackbarState() = when (this) {
    BatteryOptimizationError.NoActivityFound -> {
        SnackbarState(
            R.string.pref_snackbar_bo_no_activity_found_message,
            withDismissAction = true,
        )
    }
    BatteryOptimizationError.AlreadyDisabled -> {
        SnackbarState(
            R.string.pref_snackbar_bo_already_disabled_message,
            withDismissAction = true,
        )
    }
    BatteryOptimizationError.Unknown -> {
        SnackbarState(
            R.string.pref_snackbar_bo_unknown_error_message,
            withDismissAction = true,
        )
    }
}
