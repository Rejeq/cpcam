package com.rejeq.cpcam.feature.settings.item

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Provides a clickable item that opens a dialog for user input or selection.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param isDialogShown A boolean indicating whether the dialog is visible.
 * @param onItemClick A callback function invoked when the list item is
 *        clicked, typically to show the selection dialog.
 * @param onDismiss A callback function invoked when the dialog is dismissed.
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
    isDialogShown: Boolean,
    onItemClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dismissButton: (@Composable () -> Unit) = {},
    confirmButton: (@Composable () -> Unit) = {},
    widget: (@Composable BoxScope.() -> Unit)? = null,
    dialog: (@Composable () -> Unit) = {},
) {
    TextItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = onItemClick,
        modifier = modifier,
        widget = widget,
    )

    if (isDialogShown) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = dialog,
            dismissButton = dismissButton,
            confirmButton = confirmButton,
        )
    }
}

/**
 * Displays a clickable item that opens a dialog with [LazyListScope] content
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param selected Currently selected option text
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param content Content of the alert dialog
 * @param isDialogShown A boolean indicating whether the selection dialog is
 *        currently visible.
 * @param onDialogDismiss A callback function invoked when the selection dialog
 *        is dismissed (closed).
 * @param onItemClick A callback function invoked when the list item is
 *        clicked, typically to show the selection dialog.
 */
@Composable
fun ListDialogItem(
    title: String,
    subtitle: String,
    selected: String?,
    isDialogShown: Boolean,
    onDialogDismiss: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    DialogItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        isDialogShown = isDialogShown,
        onItemClick = onItemClick,
        onDismiss = onDialogDismiss,
        modifier = modifier,
        widget = {
            Text(
                text = selected ?: "",
                color = MaterialTheme.colorScheme.secondary,
            )
        },
    ) {
        LazyColumn(content = content)
    }
}

@Composable
fun DialogSelectableRow(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onSelect()
                    }
                },
            )
            .fillMaxWidth()
            .minimumInteractiveComponentSize(),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.merge(),
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}
