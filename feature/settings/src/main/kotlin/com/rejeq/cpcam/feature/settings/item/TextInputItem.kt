package com.rejeq.cpcam.feature.settings.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.feature.settings.input.selectAll
import kotlinx.coroutines.job

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
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val isDialogShown = rememberSaveable { mutableStateOf(false) }
    val textFocus = remember { FocusRequester() }

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
                text = value.text,
                color = MaterialTheme.colorScheme.secondary,
            )
        },
    ) {
        LaunchedEffect(Unit) {
            // We don't use requestFocus() in onItemClick(), since at the moment
            // of click event - TextField() composable is not created, so
            // there no any attached focus node, and requestFocus() crashes
            coroutineContext.job.invokeOnCompletion {
                textFocus.requestFocus()
                onValueChange(value.selectAll())
            }
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().focusRequester(textFocus),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
        )
    }
}
