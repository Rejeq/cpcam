package com.rejeq.cpcam.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.rejeq.cpcam.core.ui.LocalIsWindowFocused
import com.rejeq.cpcam.core.ui.LocalPermissionStorage
import com.rejeq.cpcam.core.ui.PermissionBlockedContent
import com.rejeq.cpcam.core.ui.PermissionState
import com.rejeq.cpcam.core.ui.rememberPermissionLauncher
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.core.ui.wantUseDarkMode
import com.rejeq.cpcam.feature.about.LibrariesContent
import com.rejeq.cpcam.feature.about.LibraryContent
import com.rejeq.cpcam.feature.main.MainContent
import com.rejeq.cpcam.feature.scanner.qr.QrScannerContent
import com.rejeq.cpcam.feature.service.ConnectionErrorContent
import com.rejeq.cpcam.feature.settings.SettingsContent
import com.rejeq.cpcam.feature.settings.endpoint.EndpointContent

@Composable
fun RootContent(
    component: RootComponent,
    onDarkModeChange: (isDarkModeEnabled: Boolean) -> Unit,
    hasWindowFocus: Boolean = true,
) {
    val useDarkMode = component.useDarkMode.collectAsState()
    val useDynamicColor = component.useDynamicColor.collectAsState()

    val wantUseDarkMode = wantUseDarkMode(useDarkMode.value)
    CpcamTheme(wantUseDarkMode, useDynamicColor.value) {
        LaunchedEffect(wantUseDarkMode, onDarkModeChange) {
            onDarkModeChange(wantUseDarkMode)
        }

        CompositionLocalProvider(
            LocalIsWindowFocused provides hasWindowFocus,
            LocalPermissionStorage provides component.permissionStorage,
        ) {
            RootChildren(component)
        }
    }
}

@Composable
@OptIn(ExperimentalDecomposeApi::class)
fun RootChildren(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier,
        stack = component.stack,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(slide()),
            onBack = component::onBackClicked,
        ),
    ) { child ->
        val dialog = component.dialog.subscribeAsState().value
        val dialogInstance = dialog.child?.instance
        dialogInstance?.let {
            when (it) {
                is RootComponent.DialogChild.ConnectionError ->
                    ConnectionErrorContent(it.component)
                is RootComponent.DialogChild.ConfirmAppRestart ->
                    ConfirmAppRestartDialogContent(it.component)
                is RootComponent.DialogChild.PermissionBlocked ->
                    PermissionBlockedContent(it.component)
            }
        }

        val endpointPerms = component.endpointService.requiredPermissions
        val endpointPermLauncher = rememberPermissionLauncher { state ->
            when (state) {
                is PermissionState.Granted -> {
                    component.onStartEndpoint()
                }
                is PermissionState.PermanentlyDenied -> {
                    // TODO: When endpoint service would required several
                    //  permissions to run, show dialog only when all permission
                    //  are permanently denied
                    component.onPermissionBlocked(endpointPerms)
                }
                is PermissionState.Denied -> { }
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            when (val child = child.instance) {
                is RootComponent.Child.Main -> MainContent(
                    component = child.component,
                    dimScreenAllowed = dialogInstance == null,
                    snackbarDispatcher = component.snackbarDispatcher,
                    onSettingsClick = component::onSettingsClick,
                    onStartEndpoint = {
                        if (!component.endpointService.hasPermissions()) {
                            endpointPermLauncher.launch(endpointPerms)
                        } else {
                            component.onStartEndpoint()
                        }
                    },
                    onStopEndpoint = component::onStopEndpoint,
                )

                is RootComponent.Child.Settings -> SettingsContent(
                    component = child.component,
                    snackbarDispatcher = component.snackbarDispatcher,
                )

                is RootComponent.Child.EndpointSettings -> EndpointContent(
                    component = child.component,
                    snackbarDispatcher = component.snackbarDispatcher,
                )

                is RootComponent.Child.QrScanner -> QrScannerContent(
                    component = child.component,
                    snackbarDispatcher = component.snackbarDispatcher,
                )

                is RootComponent.Child.Libraries -> LibrariesContent(
                    component = child.component,
                    snackbarDispatcher = component.snackbarDispatcher,
                )

                is RootComponent.Child.Library -> LibraryContent(
                    component = child.component,
                    snackbarDispatcher = component.snackbarDispatcher,
                )
            }
        }
    }
}
