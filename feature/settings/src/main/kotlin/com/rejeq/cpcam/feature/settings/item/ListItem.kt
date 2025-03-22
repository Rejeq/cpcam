package com.rejeq.cpcam.feature.settings.item

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Settings item for selecting from a list of options.
 *
 * Displays a clickable item that opens a dialog with radio buttons for
 * selection.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param selected Currently selected option text
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param alertContent Content of the alert dialog
 */
@Composable
fun ListItem(
    title: String,
    subtitle: String,
    selected: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alertContent: LazyListScope.(MutableState<Boolean>) -> Unit,
) {
    val isDialogShown = rememberSaveable { mutableStateOf(false) }

    TextItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { isDialogShown.value = true },
        modifier = modifier,
    ) {
        Text(
            text = selected ?: "",
            color = MaterialTheme.colorScheme.secondary,
        )
    }

    if (isDialogShown.value) {
        ListAlertDialog(
            title = title,
            onDismiss = { isDialogShown.value = false },
            showDialog = isDialogShown,
            alertContent = alertContent,
        )
    }
}

/**
 * Dialog for list selection.
 *
 * @param title Dialog title
 * @param onDismiss Callback when dialog is dismissed
 * @param alertContent Content of alert dialog
 */
@Composable
fun ListAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    showDialog: MutableState<Boolean>,
    alertContent: LazyListScope.(MutableState<Boolean>) -> Unit,
) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title) },
    text = {
        LazyColumn(content = { alertContent(showDialog) })
    },
    confirmButton = { },
)

@Composable
fun DialogRow(
    label: String,
    isSelected: Boolean,
    showDialog: MutableState<Boolean>,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onSelect()
                        showDialog.value = false
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
