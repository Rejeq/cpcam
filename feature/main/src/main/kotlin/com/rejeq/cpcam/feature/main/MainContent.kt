package com.rejeq.cpcam.feature.main

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.rejeq.cpcam.core.camera.ui.CameraContent
import com.rejeq.cpcam.core.camera.ui.CameraPreviewState
import com.rejeq.cpcam.core.camera.ui.SwitchCameraButton
import com.rejeq.cpcam.core.camera.ui.TorchButton
import com.rejeq.cpcam.core.device.dimScreen
import com.rejeq.cpcam.core.device.restoreScreenBrightness
import com.rejeq.cpcam.core.ui.Edge
import com.rejeq.cpcam.core.ui.LocalIsWindowFocused
import com.rejeq.cpcam.core.ui.MorphButtonState
import com.rejeq.cpcam.core.ui.MorphIconButton
import com.rejeq.cpcam.core.ui.MorphIconTarget
import com.rejeq.cpcam.core.ui.PermissionBlockedContent
import com.rejeq.cpcam.core.ui.SlideFromEdge
import com.rejeq.cpcam.core.ui.SnackbarDispatcher
import com.rejeq.cpcam.core.ui.SnackbarDispatcherContent
import com.rejeq.cpcam.core.ui.modifier.DeviceOrientation
import com.rejeq.cpcam.core.ui.modifier.ProvideDeviceOrientation
import com.rejeq.cpcam.core.ui.modifier.adaptiveRotation
import com.rejeq.cpcam.core.ui.modifier.keepScreenAwake
import com.rejeq.cpcam.core.ui.modifier.lockOrientation
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.main.info.InfoContent

@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier,
    dimScreenAllowed: Boolean = true,
    snackbarDispatcher: SnackbarDispatcher? = null,
) {
    val dialog = component.nav.dialog.subscribeAsState().value
    val dialogInstance = dialog.child?.instance

    dialogInstance?.let {
        when (it) {
            is MainNavigation.Dialog.Info -> InfoContent(it.component)

            is MainNavigation.Dialog.PermissionBlocked ->
                PermissionBlockedContent(it.component)
        }
    }

    val camState = component.cam.state.collectAsState().value
    val isWindowFocused = LocalIsWindowFocused.current
    val shouldKeepScreenAwake =
        component.keepScreenAwake.collectAsState().value &&
            camState is CameraPreviewState.Opened
    val dimScreenDelay = component.dimScreenDelay.collectAsState().value

    // Screen has unimportant content only when
    // - The user focused in our window
    // - Doesn't have any active dialog
    // - If parent composable doesn't have important content
    val hasUnimportantContent =
        isWindowFocused && dialogInstance == null && dimScreenAllowed

    MainScreenLayout(
        keepScreenAwake = hasUnimportantContent && shouldKeepScreenAwake,
        dimScreenDelay = dimScreenDelay.takeIf {
            hasUnimportantContent && shouldKeepScreenAwake
        },
        modifier = modifier,
        background = {
            CameraContent(component.cam)
        },
        top = {
            InfoBar(
                onSettingsClick = component::onSettingsClick,
                onStreamInfoClick = component.nav::showStreamInfo,
                hasStreamInfo = component.showInfoButton.collectAsState().value,
                isTorchEnabled = component.cam.isTorchEnabled
                    .collectAsState().value,
                hasTorch = component.cam.hasTorch.collectAsState().value,
                onTorchClick = component.cam::onToggleTorch,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        bottom = { bottomModifier ->
            val streamState = component.streamButtonState

            ActionBar(
                streamButtonState = streamState,
                onStreamClick = {
                    when (streamState.animTarget) {
                        MorphIconTarget.Stopped -> component.onStartEndpoint()
                        MorphIconTarget.Loading -> {}
                        MorphIconTarget.Started -> component.onStopEndpoint()
                    }
                },
                showStreamButton = component.showStreamButton
                    .collectAsState().value,
                showSwitchCameraButton = component.cam.hasMultipleCameras
                    .collectAsState().value,
                onSwitchCameraClick = component.cam::onSwitchCamera,
                modifier = bottomModifier.fillMaxWidth(),
            )
        },
        snackbar = {
            SnackbarDispatcherContent(snackbarDispatcher)
        },
    )
}

@Composable
fun MainScreenLayout(
    background: @Composable () -> Unit,
    top: @Composable () -> Unit,
    bottom: @Composable (Modifier) -> Unit,
    snackbar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    keepScreenAwake: Boolean = false,
    dimScreenDelay: Long? = null,
) {
    val actionBarPadding = 56.dp

    val window = LocalActivity.current?.window
    val lifecycle = LocalLifecycleOwner.current

    ProvideDeviceOrientation(DeviceOrientation.Portrait) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .lockOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .keepScreenAwake(keepScreenAwake)
                .detectUserActivity(
                    enabled = dimScreenDelay != null,
                    inactivityDelay = dimScreenDelay ?: 0L,
                    lifecycle = lifecycle.lifecycle,
                ) { isActive ->
                    if (window != null) {
                        if (isActive) {
                            restoreScreenBrightness(window)
                        } else {
                            dimScreen(window)
                        }
                    }
                },
        ) {
            background()

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
            ) {
                top()

                Column {
                    snackbar()
                    bottom(Modifier.padding(bottom = actionBarPadding))
                }
            }
        }
    }
}

@Composable
fun InfoBar(
    onSettingsClick: () -> Unit,
    onStreamInfoClick: () -> Unit,
    hasStreamInfo: Boolean,
    hasTorch: Boolean,
    isTorchEnabled: Boolean,
    onTorchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SlideFromEdge(
                visible = hasTorch,
                edge = Edge.Start,
            ) {
                TorchButton(
                    isEnabled = isTorchEnabled,
                    onClick = onTorchClick,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
        ) {
            SettingsButton(onClick = onSettingsClick)

            SlideFromEdge(
                visible = hasStreamInfo,
                edge = Edge.End,
            ) {
                StreamInfoButton(onClick = onStreamInfoClick)
            }
        }
    }
}

@Composable
fun ActionBar(
    streamButtonState: MorphButtonState,
    showStreamButton: Boolean,
    onStreamClick: () -> Unit,
    showSwitchCameraButton: Boolean,
    onSwitchCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowHeight = 96.dp

    Row(
        modifier = modifier.requiredHeight(rowHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TODO: Make toggle mute buttons
        Box(modifier = Modifier.requiredSize(48.dp))

        SlideFromEdge(showStreamButton, Edge.Bottom) {
            MorphIconButton(
                state = streamButtonState,
                enabled = true,
                onClick = onStreamClick,
                modifier = Modifier.requiredSize(rowHeight).adaptiveRotation(),
            )
        }

        Box(modifier = Modifier.requiredSize(48.dp)) {
            SlideFromEdge(showSwitchCameraButton, Edge.End) {
                SwitchCameraButton(onClick = onSwitchCameraClick)
            }
        }
    }
}

@Composable
@Preview
@PreviewScreenSizes
@PreviewLightDark
@PreviewDynamicColors
private fun PreviewMainContent() {
    CpcamTheme {
        Surface {
            MainContent(
                component = PreviewMainComponent(),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewInfoBar() {
    CpcamTheme {
        InfoBar(
            onSettingsClick = { },
            onStreamInfoClick = {},
            hasStreamInfo = true,
            hasTorch = true,
            isTorchEnabled = false,
            onTorchClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewActionBar() {
    CpcamTheme {
        ActionBar(
            streamButtonState = MorphButtonState(MorphIconTarget.Stopped),
            showStreamButton = true,
            onStreamClick = {},
            showSwitchCameraButton = true,
            onSwitchCameraClick = {},
        )
    }
}
