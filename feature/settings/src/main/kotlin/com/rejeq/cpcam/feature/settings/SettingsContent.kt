package com.rejeq.cpcam.feature.settings

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.CpcamTopBar
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.settings.preference.AppearanceState
import com.rejeq.cpcam.feature.settings.preference.AudioState
import com.rejeq.cpcam.feature.settings.preference.CameraState
import com.rejeq.cpcam.feature.settings.preference.PreferenceContent
import com.rejeq.cpcam.feature.settings.preference.ScreenState
import com.rejeq.cpcam.feature.settings.preference.StreamState
import com.rejeq.cpcam.feature.settings.preference.appearancePreferences
import com.rejeq.cpcam.feature.settings.preference.audioPreferences
import com.rejeq.cpcam.feature.settings.preference.cameraPreferences
import com.rejeq.cpcam.feature.settings.preference.screenPreferences
import com.rejeq.cpcam.feature.settings.preference.streamPreferences

@Composable
fun SettingsContent(
    component: SettingsComponent,
    modifier: Modifier = Modifier,
) {
    val streamTitle = stringResource(R.string.pref_group_stream)
    val streamPreferences = remember {
        makeStreamPreferences(onEndpointClick = component::onEndpointClick)
    }

    val cameraTitle = stringResource(R.string.pref_group_camera)
    val cameraPreferences = remember { makeCameraPreferences(component) }

    val audioTitle = stringResource(R.string.pref_group_audio)
    val audioPreferences = remember { makeAudioPreferences() }

    val screenTitle = stringResource(R.string.pref_group_screen)
    val screenPreferences = remember { makeScreenPreferences(component) }

    val appearanceTitle = stringResource(R.string.pref_group_appearance)
    val appearancePreferences =
        remember { makeAppearancePreferences(component) }

    SettingsLayout(
        modifier = modifier,
        topBar = {
            CpcamTopBar(
                title = stringResource(R.string.settings_screen_title),
                onBackClick = component::onFinished,
            )
        },
        content = {
            preferenceGroup(streamTitle, streamPreferences)
            preferenceGroup(cameraTitle, cameraPreferences)
            preferenceGroup(audioTitle, audioPreferences)
            preferenceGroup(screenTitle, screenPreferences)
            preferenceGroup(appearanceTitle, appearancePreferences)
        },
        footer = {
            AboutApp(
                versionName = component.versionName,
                onLicenseClick = component::onLibraryLicensesClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))
        },
    )
}

fun makeStreamPreferences(
    onEndpointClick: () -> Unit,
): List<PreferenceContent> {
    val state = StreamState(
        onEndpointClick = onEndpointClick,
    )

    return streamPreferences(state)
}

fun makeCameraPreferences(
    component: SettingsComponent,
): List<PreferenceContent> {
    val state = CameraState(
        availableResolution = component.availableResolution,
        selectedResolution = component.selectedResolution,
        onResolutionChange = component::onCameraResolutionChange,
        availableFramerates = component.availableFramerates,
        selectedFramerate = component.selectedFramerate,
        onFramerateChange = component::onCameraFramerateChange,
    )

    return cameraPreferences(state)
}

fun makeAudioPreferences(): List<PreferenceContent> {
    val state = AudioState(
        onInputSourceClick = { Log.i("LOGITS", "Clicked") },
        onRateClick = { Log.i("LOGITS", "Clicked") },
        onNoiseSuppressionClick = { Log.i("LOGITS", "Clicked") },
    )

    return audioPreferences(state)
}

fun makeScreenPreferences(
    component: SettingsComponent,
): List<PreferenceContent> {
    val state = ScreenState(
        keepScreenAwake = component.keepScreenAwake,
        onKeepScreenAwakeChange = component::onKeepScreenAwakeChange,
        dimScreenDelay = component.dimScreenDelay,
        onDimScreenChange = component::onDimScreenDelayChange,
    )

    return screenPreferences(state)
}

fun makeAppearancePreferences(
    component: SettingsComponent,
): List<PreferenceContent> {
    val state = AppearanceState(
        selectedTheme = component.themeConfig,
        onThemeChange = component::onThemeConfigChange,
        useDynamicColor = component.useDynamicColor,
        onDynamicColorChange = component::onUseDynamicColorChange,
    )

    return appearancePreferences(state)
}

fun LazyListScope.preferenceGroup(
    title: String,
    items: List<PreferenceContent>,
) {
    item {
        PreferenceGroupHeader(
            title = title,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }

    items(items) { content ->
        content(
            Modifier.padding(
                top = 14.dp,
                bottom = 14.dp,
                start = 32.dp,
                end = 16.dp,
            ),
        )
    }

    item {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun PreferenceGroupHeader(title: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
@PreviewScreenSizes
@PreviewLightDark
fun PreviewSettingsContent() {
    CpcamTheme {
        SettingsContent(
            component = PreviewSettingsComponent(),
        )
    }
}
