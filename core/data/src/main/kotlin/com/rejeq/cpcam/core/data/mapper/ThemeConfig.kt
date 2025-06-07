package com.rejeq.cpcam.core.data.mapper

import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.data.datastore.DynamicColorProto
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

fun DynamicColorProto?.fromDataStore() = when (this) {
    null,
    DynamicColorProto.UNRECOGNIZED,
    DynamicColorProto.DYNAMIC_COLOR_UNSPECIFIED,
    DynamicColorProto.DYNAMIC_COLOR_ENABLE,
    -> {
        true
    }
    DynamicColorProto.DYNAMIC_COLOR_DISABLE,
    -> {
        false
    }
}

internal fun Boolean.toDynamicColorProto() = if (this) {
    DynamicColorProto.DYNAMIC_COLOR_ENABLE
} else {
    DynamicColorProto.DYNAMIC_COLOR_DISABLE
}
