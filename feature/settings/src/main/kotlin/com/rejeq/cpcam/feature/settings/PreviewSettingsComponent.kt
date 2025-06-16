package com.rejeq.cpcam.feature.settings

import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.Framerate
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.device.Locale
import com.rejeq.cpcam.core.device.R as DeviceR
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewSettingsComponent : SettingsComponent {
    override val themeConfig = MutableStateFlow(ThemeConfig.FOLLOW_SYSTEM)
    override val useDynamicColor = MutableStateFlow(true)
    override val currentLocale = MutableStateFlow(
        Locale("en", DeviceR.string.locale_en),
    )
    override val selectedResolution = MutableStateFlow<Resolution?>(null)
    override val availableResolution = MutableStateFlow(
        listOf(
            Resolution(1920, 1080),
            Resolution(1280, 720),
            Resolution(640, 480),
        ),
    )
    override val selectedFramerate = MutableStateFlow<Framerate?>(null)
    override val availableFramerates = MutableStateFlow(
        listOf(
            Framerate(30, 30),
            Framerate(30, 60),
            Framerate(60, 60),
        ),
    )
    override val keepScreenAwake = MutableStateFlow(false)
    override val dimScreenDelay = MutableStateFlow(TextFieldValue("30000"))
    override val versionName = "1.0.0-preview"

    override fun onThemeConfigChange(themeConfig: ThemeConfig) {
        this.themeConfig.value = themeConfig
    }

    override fun onUseDynamicColorChange(needUse: Boolean) {
        useDynamicColor.value = needUse
    }

    override fun onLocaleChange(newLocale: Locale) {
        currentLocale.value = newLocale
    }

    override fun onCameraResolutionChange(resolution: Resolution?) {
        selectedResolution.value = resolution
    }

    override fun onCameraFramerateChange(framerate: Framerate?) {
        selectedFramerate.value = framerate
    }

    override fun onKeepScreenAwakeChange(enabled: Boolean) {
        keepScreenAwake.value = enabled
    }

    override fun onDimScreenDelayChange(time: TextFieldValue) {
        dimScreenDelay.value = time
    }

    override fun onFinished() { }

    override fun onLibraryLicensesClick() { }

    override fun onEndpointClick() { }
}
