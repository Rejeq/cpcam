package com.rejeq.cpcam.feature.settings.item

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Settings item with a toggle switch.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param checked Current state of the switch
 * @param onValueChange Callback when switch state changes
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 */
@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { onValueChange(!checked) },
        modifier = modifier,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}
