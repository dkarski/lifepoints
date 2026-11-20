package dev.dkarski.tcglivecounter.data

import android.content.Context
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/** App preferences: theme and language. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.Light
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    /** BCP 47 tag of the chosen app language (see [AppLanguages]); null = follow the system language. */
    var languageTag: String?
        get() = prefs.getString(KEY_LANGUAGE, null)?.takeIf { AppLanguages.find(it) != null }
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, value) }.apply()

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_LANGUAGE = "language"
    }
}
