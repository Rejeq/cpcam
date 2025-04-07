package com.rejeq.cpcam.feature.settings.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

/**
 * Settings item that opens a dialog with a text input field.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param value The current value of the text input
 * @param onValueChange Callback when the text input value changes
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param keyboardOptions Options for the keyboard input
 * @param keyboardActions Actions for keyboard events
 */
@Composable
fun TextInputItem(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isDialogShown = rememberSaveable { mutableStateOf(false) }

    DialogItem(
        title = title,
        subtitle = subtitle,
        isDialogShown = isDialogShown.value,
        onItemClick = { isDialogShown.value = true },
        onDismiss = { isDialogShown.value = false },
        modifier = modifier,
        enabled = enabled,
        widget = {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.secondary,
            )
        },
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
        )
    }
}
