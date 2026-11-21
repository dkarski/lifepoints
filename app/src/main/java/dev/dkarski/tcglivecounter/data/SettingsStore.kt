package dev.dkarski.tcglivecounter.data

import android.content.Context

/** App preferences: language and vibration. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    init {
        // Up to 1.1 the app had a Light/Dark theme; the choice is no longer used. The other settings stay.
        if (prefs.contains(KEY_LEGACY_THEME)) prefs.edit().remove(KEY_LEGACY_THEME).apply()
    }

    /** Vibration on life changes (also off when touch vibration is off in the phone settings). */
    var vibration: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    /** BCP 47 tag of the chosen app language (see [AppLanguages]); null = follow the system language. */
    var languageTag: String?
        get() = prefs.getString(KEY_LANGUAGE, null)?.takeIf { AppLanguages.find(it) != null }
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_LANGUAGE) else putString(KEY_LANGUAGE, value) }.apply()

    private companion object {
        const val KEY_LEGACY_THEME = "theme"
        const val KEY_LANGUAGE = "language"
        const val KEY_VIBRATION = "vibration"
    }
}
