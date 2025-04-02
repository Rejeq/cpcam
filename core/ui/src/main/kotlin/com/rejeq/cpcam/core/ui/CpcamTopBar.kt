package com.rejeq.cpcam.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.rejeq.cpcam.core.ui.theme.CpcamTheme

/**
 * A consistent top app bar implementation for the app
 *
 * This composable provides a standardized top bar with:
 * - Title with proper styling and ellipsis handling
 * - Back navigation button
 * - Optional actions slot for additional buttons
 *
 * @param title The text to display in the top bar
 * @param onBackClick Callback invoked when the back button is clicked
 * @param modifier Optional modifier for customizing the top bar
 * @param actions Optional composable for adding action buttons to the top bar
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CpcamTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                val painter = painterResource(R.drawable.ic_arrow_back_24dp)
                val desc = stringResource(R.string.btn_back_screen_desc)

                Icon(painter, desc)
            }
        },
        actions = actions,
    )
}

@Preview
@Composable
private fun PreviewCpcamTopBar() {
    CpcamTheme {
        CpcamTopBar(title = "Title", onBackClick = {}) {

            Button(onClick = {}) {
                Text("Action 2")
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCpcamTopBarLongTitle() {
    CpcamTheme {
        CpcamTopBar(
            title = "This is very very very very very long title",
            onBackClick = { },
        ) {
            Button(onClick = {}) {
                Text("Action 1")
            }

            Button(onClick = {}) {
                Text("Action 2")
            }
        }
    }
}
