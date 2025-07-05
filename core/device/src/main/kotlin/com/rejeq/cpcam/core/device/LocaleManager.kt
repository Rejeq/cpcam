package com.rejeq.cpcam.core.device

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Sets the application's locale to the specified language tag.
 *
 * This function uses [AppCompatDelegate.setApplicationLocales] to update
 * the current application language. The provided [locale] should be a valid
 * IETF BCP 47 language tag (e.g., "en", "ru", "en-US").
 *
 * Note: This API should always be called after `Activity.onCreate()`,
 * see [AppCompatDelegate.setApplicationLocales] for details.
 *
 * @param locale The language tag to set as the application's locale.
 *        If null, the system default locale will be used.
 */
fun setAppLocale(locale: String?) {
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(locale),
    )
}

/**
 * Retrieves the currently set application locale as a [Locale] object.
 * If no locale is currently set, it returns the default locale.
 *
 * Note: This API should always be called after `Activity.onCreate()`,
 * see [AppCompatDelegate.getApplicationLocales] for details.
 *
 * @return The current [Locale] used by the application,
 *         or null if no match is found.
 */
fun getCurrentAppLocale(): Locale? {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales.isEmpty) {
        return APPLICATION_LOCALES.first()
    }

    return locales.toLanguageTags().split(',').firstNotNullOfOrNull { tag ->
        APPLICATION_LOCALES.find { it.tag == tag }
    }
}

/**
 * A list of supported application locales.
 */
val APPLICATION_LOCALES: Array<Locale> by lazy {
    arrayOf(
        // NOTE: First element must be system default,
        // since [getCurrentAppLocale] returns first entry by default.
        Locale(null, R.string.locale_system_default),
        Locale("en", R.string.locale_en),
        Locale("ru", R.string.locale_ru),
    )
}

/**
 * Represents a supported application locale.
 *
 * @property tag A BCP 47 language tag (e.g., "en", "ru").
 *        If null, it represents the system default locale.
 * @property labelId A string resource ID that corresponds to the locale label.
 */
class Locale(val tag: String?, val labelId: Int)
