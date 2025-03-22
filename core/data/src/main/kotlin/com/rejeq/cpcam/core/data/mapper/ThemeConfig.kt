package com.rejeq.cpcam.core.data.mapper

import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.data.datastore.ThemeConfigProto

fun ThemeConfigProto?.fromDataStore() = when (this) {
    null,
    ThemeConfigProto.UNRECOGNIZED,
    ThemeConfigProto.THEME_CONFIG_UNSPECIFIED,
    ThemeConfigProto.THEME_CONFIG_FOLLOW_SYSTEM,
    -> {
        ThemeConfig.FOLLOW_SYSTEM
    }

    ThemeConfigProto.THEME_CONFIG_LIGHT -> {
        ThemeConfig.LIGHT
    }

    ThemeConfigProto.THEME_CONFIG_DARK -> {
        ThemeConfig.DARK
    }
}

fun ThemeConfig.toDataStore() = when (this) {
    ThemeConfig.FOLLOW_SYSTEM -> {
        ThemeConfigProto.THEME_CONFIG_FOLLOW_SYSTEM
    }

    ThemeConfig.LIGHT -> {
        ThemeConfigProto.THEME_CONFIG_LIGHT
    }

    ThemeConfig.DARK -> {
        ThemeConfigProto.THEME_CONFIG_DARK
    }
}
