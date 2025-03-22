package com.rejeq.cpcam.feature.main

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.rejeq.cpcam.core.ui.DeviceOrientation
import com.rejeq.cpcam.core.ui.Edge
import com.rejeq.cpcam.core.ui.MorphButtonState
import com.rejeq.cpcam.core.ui.MorphIconButton
import com.rejeq.cpcam.core.ui.MorphIconTarget
import com.rejeq.cpcam.core.ui.ProvideDeviceOrientation
import com.rejeq.cpcam.core.ui.SlideFromEdge
import com.rejeq.cpcam.core.ui.adaptiveRotation
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.main.camera.CameraContent
import com.rejeq.cpcam.feature.main.info.InfoContent

@Composable
fun MainContent(component: MainComponent, modifier: Modifier = Modifier) {
    MainScreenLayout(
        modifier = modifier.fillMaxSize(),
        background = {
            CameraContent(component.cam)
        },
        top = {
            val hasStreamInfo = component.showInfoButton.collectAsState(false)

            InfoBar(
                onSettingsClick = component.onSettingsClick,
                onStreamInfoClick = component.nav::showStreamInfo,
                hasStreamInfo = hasStreamInfo.value,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        bottom = { bottomModifier ->
            val streamState = component.streamButtonState
            val showStreamButton = component.showStreamButton
                .collectAsState(false)
            val showSwitchCameraButton = component.showSwitchCameraButton
                .collectAsState(false)

            ActionBar(
                streamButtonState = streamState,
                showStreamButton = showStreamButton.value,
                onStreamClick = {
                    when (streamState.animTarget) {
                        MorphIconTarget.Stopped -> component.connect()
                        MorphIconTarget.Loading -> {}
                        MorphIconTarget.Started -> component.disconnect()
                    }
                },
                showSwitchCameraButton = showSwitchCameraButton.value,
                onSwitchCameraClick = component.cam::switchCamera,
                modifier = bottomModifier.fillMaxWidth(),
            )
        },
    )

    val dialog = component.nav.dialog.subscribeAsState().value
    dialog.child?.instance?.let {
        when (it) {
            is MainNavigation.Dialog.Info -> InfoContent(it.component)
            is MainNavigation.Dialog.PermanentNotification ->
                PermissionDeniedContent(it.component)
        }
    }
}

@Composable
fun MainScreenLayout(
    background: @Composable () -> Unit,
    top: @Composable () -> Unit,
    bottom: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionBarPadding = 56.dp

    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        activity?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {
            activity?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    ProvideDeviceOrientation(DeviceOrientation.Portrait) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier,
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

                bottom(Modifier.padding(bottom = actionBarPadding))
            }
        }
    }
}

@Composable
fun InfoBar(
    onSettingsClick: () -> Unit,
    onStreamInfoClick: () -> Unit,
    hasStreamInfo: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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

@Preview
@Composable
private fun PreviewInfoBar() {
    CpcamTheme {
        InfoBar(
            onSettingsClick = { },
            onStreamInfoClick = {},
            hasStreamInfo = true,
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
