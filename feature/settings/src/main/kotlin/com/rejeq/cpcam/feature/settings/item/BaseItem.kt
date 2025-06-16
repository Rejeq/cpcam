package com.rejeq.cpcam.feature.settings.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Base composable for settings items.
 *
 * Provides the foundation layout for all settings items with consistent styling
 * and behavior.
 *
 * @param title The primary text to display
 * @param subcomponent Composable for additional content below the title
 * @param modifier Optional modifier for customizing the layout
 * @param enabled Whether the item is interactive
 * @param onClick Optional callback for click events
 * @param widget Optional composable for the end of the item (e.g., Switch)
 */
@Composable
fun BaseItem(
    title: String,
    subcomponent: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    widget: (@Composable BoxScope.() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = { onClick?.invoke() })
            .graphicsLayer {
                if (!enabled) {
                    alpha = DISABLED_COLOR_OPACITY
                }
            }
            .then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                )
            }

            subcomponent.invoke(this)
        }

        if (widget != null) {
            Box(content = widget)
        }
    }
}

private const val DISABLED_COLOR_OPACITY = 0.38f
