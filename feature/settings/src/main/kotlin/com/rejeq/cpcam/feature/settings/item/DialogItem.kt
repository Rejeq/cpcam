package com.rejeq.cpcam.feature.settings.item

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

/**
 * Settings item that displays a dialog for input.
 *
 * Provides a clickable settings item that opens a dialog for user input
 * or selection. The dialog state is preserved across configuration changes.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param dismissButton Composable for the dialog's dismiss button
 * @param confirmButton Composable for the dialog's confirm button
 * @param dialog Composable for the dialog's content
 */
@Composable
fun DialogItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dismissButton: (@Composable () -> Unit) = {},
    confirmButton: (@Composable () -> Unit) = {},
    dialog: (@Composable () -> Unit) = {},
) {
    val isDialogShown = rememberSaveable { mutableStateOf(false) }

    TextItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { isDialogShown.value = true },
        modifier = modifier,
    )

    if (isDialogShown.value) {
        AlertDialog(
            onDismissRequest = {
                isDialogShown.value = false
            },
            title = { Text(text = title) },
            text = dialog,
            dismissButton = dismissButton,
            confirmButton = confirmButton,
        )
    }
}
