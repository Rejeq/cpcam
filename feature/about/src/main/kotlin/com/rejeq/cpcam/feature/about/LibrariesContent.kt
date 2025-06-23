package com.rejeq.cpcam.feature.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.rejeq.cpcam.core.ui.CpcamTopBar
import com.rejeq.cpcam.core.ui.SnackbarDispatcher
import com.rejeq.cpcam.core.ui.SnackbarDispatcherContent

@Composable
fun LibrariesContent(
    component: LibrariesComponent,
    modifier: Modifier = Modifier,
    snackbarDispatcher: SnackbarDispatcher? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CpcamTopBar(
                title = stringResource(R.string.licenses_screen_title),
                onBackClick = component.onFinished,
            )
        },
        snackbarHost = {
            SnackbarDispatcherContent(snackbarDispatcher)
        },
    ) { padding ->
        LibrariesContainer(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            onLibraryClick = component::onLibraryClick,
        )
    }
}
