package com.rejeq.cpcam.feature.settings.endpoint.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.feature.settings.R

@Composable
fun <T> FormContent(
    state: FormState<T>,
    title: String,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    isExpanded: Boolean = true,
    onHeaderClick: (() -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            FormHeader(
                title = title,
                expandable = expandable,
                isExpanded = isExpanded,
                onClick = { onHeaderClick?.invoke() },
                modifier = Modifier.fillMaxWidth(),
            )

            when (state) {
                is FormState.Loading -> { }
                is FormState.Success -> {
                    AnimatedVisibility(visible = isExpanded) {
                        content(state.data)
                    }
                }
            }
        }
    }
}

@Composable
fun FormHeader(
    title: String,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    isExpanded: Boolean = true,
    onClick: () -> Unit,
) {
    val modifier = if (expandable) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        if (expandable) {
            ExpandIcon(
                expanded = isExpanded,
            )
        }
    }
}

@Composable
fun ExpandIcon(expanded: Boolean) {
    Icon(
        painter = if (expanded) {
            painterResource(CoreR.drawable.ic_keyboard_arrow_up_24dp)
        } else {
            painterResource(CoreR.drawable.ic_keyboard_arrow_down_24dp)
        },
        contentDescription = if (expanded) {
            stringResource(R.string.endpoint_expand_btn_desc)
        } else {
            stringResource(R.string.endpoint_collapse_btn_desc)
        },
    )
}
