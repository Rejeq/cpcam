package com.rejeq.cpcam.feature.settings.item

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Settings item for displaying text with optional interaction.
 *
 * Provides a settings item with title and subtitle text, optionally supporting
 * click interactions and additional widgets.
 *
 * @param title The primary text to display
 * @param subtitle The secondary text to display
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param onClick Optional callback for click events
 * @param widget Optional composable for the end of the item
 */
@Composable
fun TextItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    widget: (@Composable BoxScope.() -> Unit)? = null,
) {
    BaseItem(
        title = title,
        subcomponent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        enabled = enabled,
        onClick = onClick,
        widget = widget,
        modifier = modifier,
    )
}
