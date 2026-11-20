package dev.dkarski.tcglivecounter.data

import java.text.Normalizer
import java.util.Locale

/**
 * A language the app is translated into.
 *
 * [tag] is the BCP 47 tag used to build the [Locale]; Android maps it to the matching `values-*` folder
 * (e.g. "he" -> values-iw, "id" -> values-in, "zh-Hans" -> values-b+zh+Hans).
 * [nativeName] is the language's own name, shown in the list: every speaker can read their own language,
 * whatever language the app is currently in. [englishName] is used only for search.
 */
data class AppLanguage(val tag: String, val nativeName: String, val englishName: String) {
    val locale: Locale get() = Locale.forLanguageTag(tag)
}

object AppLanguages {
    /** The 9 MuditaOS system languages, in the order of the system Settings > System Language screen. */
    val system: List<AppLanguage> = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("it", "Italiano", "Italian"),
        AppLanguage("pl", "Polski", "Polish"),
        AppLanguage("cs", "Čeština", "Czech"),
        AppLanguage("pt", "Português", "Portuguese"),
        AppLanguage("es", "Español", "Spanish"),
        AppLanguage("de", "Deutsch", "German"),
        AppLanguage("fr", "Français", "French"),
        AppLanguage("nl", "Nederlands", "Dutch"),
    )

    /**
     * The other 54 languages of the InkApps list: grouped by script (Latin, Greek, Cyrillic, Georgian, Hebrew,
     * Arabic, Indic, Southeast Asian, CJK) and alphabetical within each group.
     */
    val more: List<AppLanguage> = listOf(
        // Latin
        AppLanguage("id", "Bahasa Indonesia", "Indonesian"),
        AppLanguage("ms", "Bahasa Melayu", "Malay"),
        AppLanguage("ca", "Català", "Catalan"),
        AppLanguage("da", "Dansk", "Danish"),
        AppLanguage("et", "Eesti", "Estonian"),
        AppLanguage("fil", "Filipino", "Filipino"),
        AppLanguage("gl", "Galego", "Galician"),
        AppLanguage("hr", "Hrvatski", "Croatian"),
        AppLanguage("is", "Íslenska", "Icelandic"),
        AppLanguage("sw", "Kiswahili", "Swahili"),
        AppLanguage("lv", "Latviešu", "Latvian"),
        AppLanguage("lt", "Lietuvių", "Lithuanian"),
        AppLanguage("hu", "Magyar", "Hungarian"),
        AppLanguage("nb", "Norsk bokmål", "Norwegian"),
        AppLanguage("ro", "Română", "Romanian"),
        AppLanguage("rm", "Rumantsch", "Romansh"),
        AppLanguage("sk", "Slovenčina", "Slovak"),
        AppLanguage("sl", "Slovenščina", "Slovenian"),
        AppLanguage("fi", "Suomi", "Finnish"),
        AppLanguage("sv", "Svenska", "Swedish"),
        AppLanguage("vi", "Tiếng Việt", "Vietnamese"),
        AppLanguage("tr", "Türkçe", "Turkish"),
        // Greek
        AppLanguage("el", "Ελληνικά", "Greek"),
        // Cyrillic
        AppLanguage("be", "Беларуская", "Belarusian"),
        AppLanguage("bg", "Български", "Bulgarian"),
        AppLanguage("kk", "Қазақ тілі", "Kazakh"),
        AppLanguage("ky", "Кыргызча", "Kyrgyz"),
        AppLanguage("mk", "Македонски", "Macedonian"),
        AppLanguage("mn", "Монгол", "Mongolian"),
        AppLanguage("ru", "Русский", "Russian"),
        AppLanguage("sr", "Српски", "Serbian"),
        AppLanguage("uk", "Українська", "Ukrainian"),
        // Georgian, Hebrew, Arabic script
        AppLanguage("ka", "ქართული", "Georgian"),
        AppLanguage("he", "עברית", "Hebrew"),
        AppLanguage("ur", "اردو", "Urdu"),
        AppLanguage("fa", "فارسی", "Persian"),
        // Indic
        AppLanguage("ne", "नेपाली", "Nepali"),
        AppLanguage("mr", "मराठी", "Marathi"),
        AppLanguage("hi", "हिन्दी", "Hindi"),
        AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati"),
        AppLanguage("ta", "தமிழ்", "Tamil"),
        AppLanguage("te", "తెలుగు", "Telugu"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada"),
        AppLanguage("ml", "മലയാളം", "Malayalam"),
        AppLanguage("si", "සිංහල", "Sinhala"),
        // Southeast Asian
        AppLanguage("th", "ไทย", "Thai"),
        AppLanguage("lo", "ລາວ", "Lao"),
        AppLanguage("my", "မြန်မာ", "Burmese"),
        AppLanguage("km", "ខ្មែរ", "Khmer"),
        // CJK
        AppLanguage("zh-Hans", "中文（简体）", "Chinese Simplified"),
        AppLanguage("zh-Hant", "中文（繁體）", "Chinese Traditional"),
        AppLanguage("ja", "日本語", "Japanese"),
        AppLanguage("ko", "한국어", "Korean"),
    )

    val all: List<AppLanguage> = system + more

    fun find(tag: String?): AppLanguage? = tag?.let { t -> all.firstOrNull { it.tag == t } }

    /**
     * Languages matching [query] by native name, English name, name in [uiLocale] or tag.
     * Case- and accent-insensitive ("cestina" finds "Čeština"). Runs only while the user types a query.
     */
    fun search(query: String, uiLocale: Locale): List<AppLanguage> {
        val q = fold(query.trim())
        if (q.isEmpty()) return emptyList()
        return all.filter { lang ->
            listOf(lang.nativeName, lang.englishName, lang.locale.getDisplayName(uiLocale), lang.tag)
                .any { fold(it).contains(q) }
        }
    }

    private fun fold(s: String): String =
        Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(MARKS, "")

    private val MARKS = Regex("\\p{Mn}+")
}
