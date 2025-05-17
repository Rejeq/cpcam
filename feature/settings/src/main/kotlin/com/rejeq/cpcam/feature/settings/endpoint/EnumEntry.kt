package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem

@Composable
inline fun <T : Enum<T>> EnumEntry(
    title: String,
    subtitle: String,
    entries: List<T>,
    selected: T?,
    crossinline onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    ListDialogItem(
        title = title,
        subtitle = subtitle,
        selected = selected?.toString() ?: "",
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogSelectableRow(
                    label = entry.toString(),
                    isSelected = (entries.indexOf(selected) == idx),
                    onSelect = {
                        onChange(entry)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}
