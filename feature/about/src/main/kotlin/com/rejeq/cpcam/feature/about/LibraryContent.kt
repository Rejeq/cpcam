package com.rejeq.cpcam.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.CpcamTopBar
import com.rejeq.cpcam.core.ui.R as CoreR

@Composable
fun LibraryContent(component: LibraryComponent, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val state = component.state

    Scaffold(
        modifier = modifier,
        topBar = {
            CpcamTopBar(
                title = state.name,
                onBackClick = component.onFinished,
                actions = {
                    Actions(
                        website = state.website,
                        onWebsiteClick = { website ->
                            uriHandler.openUri(website)
                        },
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            items(state.licenses) { license ->
                LicenseHeader(
                    name = license.name,
                    url = license.url,
                    onUrlClick = { url ->
                        uriHandler.openUri(url)
                    },
                )

                if (license.htmlContent != null) {
                    // It's fine here to generate annotated string at every
                    // recomposition, since the state is immutable and
                    // configuration changes should be rare
                    Text(
                        text = AnnotatedString.fromHtml(
                            htmlString = license.htmlContent,
                        ),
                    )
                } else {
                    Text(text = stringResource(R.string.unable_to_open_license))
                }
            }
        }
    }
}

@Composable
fun Actions(
    website: String?,
    onWebsiteClick: (website: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (website != null) {
        IconButton(
            onClick = { onWebsiteClick(website) },
            modifier = modifier,
        ) {
            val painter = painterResource(CoreR.drawable.ic_open_in_new_24dp)
            val desc = stringResource(R.string.open_library_url_btn_desc)

            Icon(painter, desc)
        }
    }
}

@Composable
fun LicenseHeader(
    name: String,
    url: String?,
    onUrlClick: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )

        if (url != null) {
            IconButton(onClick = { onUrlClick(url) }) {
                val painter = painterResource(CoreR.drawable.ic_public_24dp)
                val desc = stringResource(R.string.open_license_url_btn_desc)

                Icon(painter, desc)
            }
        }
    }
}
