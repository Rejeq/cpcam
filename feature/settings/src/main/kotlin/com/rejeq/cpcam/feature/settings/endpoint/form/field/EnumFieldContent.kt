package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem

@Composable
fun <T> EnumFieldContent(
    state: EnumFieldState<T>,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val selected = state.selected

    ListDialogItem(
        title = title,
        subtitle = subtitle,
        selected = selected?.toString() ?: "",
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        val entries = state.availables
        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogSelectableRow(
                    label = entry.toString(),
                    isSelected = (entries.indexOf(selected) == idx),
                    onSelect = {
                        state.onSelectedChange(entry)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}
