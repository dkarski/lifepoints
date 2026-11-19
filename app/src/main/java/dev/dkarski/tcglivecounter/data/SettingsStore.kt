package dev.dkarski.tcglivecounter.data

import android.content.Context
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/** App preferences (currently only the theme). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.Light
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    private companion object {
        const val KEY_THEME = "theme"
    }
}
