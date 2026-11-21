package dev.dkarski.tcglivecounter.debugtools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.ui.theme.AppTheme

/**
 * Debug-only screen for the language feasibility audit (Lifepoints 1.1.0).
 *
 * Renders sample UI phrases of each script through the app's own MMD typography, so the screenshot shows
 * exactly what the app would get: Lato where it has glyphs, the system font fallback elsewhere.
 * Each sample is shown in the app's small body style and in bold title style. RTL samples are laid out RTL.
 *
 * Usage: adb shell am start -n dev.dkarski.lifepoints.debug/dev.dkarski.tcglivecounter.debugtools.ScriptTestActivity --es group <name>
 */
class ScriptTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = intent.getStringExtra("group") ?: "latin-ok"
        val samples = GROUPS[group] ?: GROUPS.getValue("latin-ok")
        setContent {
            AppTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        TextMMD("group: $group", style = MaterialTheme.typography.labelSmall)
                        samples.forEach { SampleRow(it) }
                    }
                }
            }
        }
    }
}

private class Sample(val tag: String, val name: String, val text: String, val rtl: Boolean = false)

@Composable
private fun SampleRow(s: Sample) {
    val locale = LocaleList(Locale(s.tag))
    CompositionLocalProvider(LocalLayoutDirection provides if (s.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Column(Modifier.fillMaxWidth()) {
            HorizontalDividerMMD()
            TextMMD("${s.tag} · ${s.name}", style = MaterialTheme.typography.labelSmall)
            TextMMD(s.text, style = MaterialTheme.typography.bodyLarge.copy(localeList = locale))
            TextMMD(
                s.text.substringBefore(" – "),
                style = MaterialTheme.typography.titleLarge.copy(localeList = locale, fontWeight = FontWeight.Bold),
            )
        }
    }
}

// Samples: a UI phrase ("Start game – Settings") plus characters typical for the language.
private val GROUPS: Map<String, List<Sample>> = mapOf(
    "latin-ok" to listOf(
        Sample("pl", "Polish", "Rozpocznij grę – Ustawienia – zażółć gęślą jaźń"),
        Sample("de", "German", "Spiel starten – Einstellungen – Größe Übung"),
        Sample("is", "Icelandic", "Hefja leik – Stillingar – þð æö ýá"),
        Sample("fr", "French", "Démarrer la partie – Paramètres – œuvre çà"),
        Sample("pt", "Portuguese", "Iniciar jogo – Definições – ação vovô"),
    ),
    "latin-c" to listOf(
        Sample("ca", "Catalan", "Inicia la partida – Configuració – col·lecció ŀ"),
        Sample("da", "Danish", "Start spil – Indstillinger – æble øl på"),
        Sample("nl", "Dutch", "Spel starten – Instellingen – één coördinaat ĳ"),
        Sample("et", "Estonian", "Alusta mängu – Seaded – šokolaad žürii õ"),
        Sample("fi", "Finnish", "Aloita peli – Asetukset – äiti öljy Åland"),
    ),
    "latin-d" to listOf(
        Sample("en", "English", "Start game – Settings – life points"),
        Sample("gl", "Galician", "Comezar partida – Axustes – ñandú unha"),
        Sample("it", "Italian", "Inizia partita – Impostazioni – perché città"),
        Sample("nb", "Norwegian", "Start spill – Innstillinger – blåbær øl"),
        Sample("rm", "Romansh", "Cumenzar il gieu – Parameters – rumantsch ü"),
        Sample("es", "Spanish", "Iniciar partida – Ajustes – ¿año? ¡señor!"),
    ),
    "latin-e" to listOf(
        Sample("sv", "Swedish", "Starta spel – Inställningar – åäö"),
        Sample("fil", "Filipino", "Simulan ang laro – Mga setting – Señora"),
        Sample("in", "Indonesian", "Mulai permainan – Pengaturan"),
        Sample("ms", "Malay", "Mula permainan – Tetapan"),
        Sample("sw", "Swahili", "Anza mchezo – Mipangilio"),
    ),
    "latin-a" to listOf(
        Sample("cs", "Czech", "Začít hru – Nastavení – Příliš žluťoučký kůň úpěl ďábelské ódy"),
        Sample("sk", "Slovak", "Začať hru – Nastavenia – Kŕdeľ ďatľov mĺkveho koňa ľ ĺ ŕ"),
        Sample("hu", "Hungarian", "Játék indítása – Beállítások – árvíztűrő tükörfúrógép ő ű"),
        Sample("ro", "Romanian", "Începe jocul – Setări – pițigăiat ș ț ă î"),
        Sample("tr", "Turkish", "Oyunu başlat – Ayarlar – İstanbul ığüşöç Ğ"),
    ),
    "latin-b" to listOf(
        Sample("lv", "Latvian", "Sākt spēli – Iestatījumi – glāžšķūņa rūķīši ģ ļ ņ"),
        Sample("lt", "Lithuanian", "Pradėti žaidimą – Nustatymai – įlinkdama ų ū ė"),
        Sample("hr", "Croatian", "Započni igru – Postavke – đačić džep"),
        Sample("sl", "Slovenian", "Začni igro – Nastavitve – šerif žganci"),
        Sample("vi", "Vietnamese", "Bắt đầu trò chơi – Cài đặt – Tiếng Việt ưở ỹ"),
    ),
    "cyr-a" to listOf(
        Sample("ru", "Russian", "Начать игру – Настройки – съешь же ещё"),
        Sample("uk", "Ukrainian", "Почати гру – Налаштування – ґ є ї"),
        Sample("be", "Belarusian", "Пачаць гульню – Налады – ў і"),
        Sample("bg", "Bulgarian", "Започни игра – Настройки – ъ щ ю"),
        Sample("mk", "Macedonian", "Започни игра – Поставки – ѓ ќ ѕ џ"),
    ),
    "cyr-b" to listOf(
        Sample("sr", "Serbian", "Започни игру – Подешавања – ђ ћ љ њ џ"),
        Sample("kk", "Kazakh", "Ойынды бастау – Баптаулар – ә ғ қ ң ө ұ ү һ і"),
        Sample("ky", "Kyrgyz", "Оюнду баштоо – Жөндөөлөр – ң ө ү"),
        Sample("mn", "Mongolian", "Тоглоом эхлүүлэх – Тохиргоо – ө ү"),
        Sample("el", "Greek", "Έναρξη παιχνιδιού – Ρυθμίσεις – ψυχοφθόρα"),
    ),
    "rtl" to listOf(
        Sample("iw", "Hebrew", "התחל משחק – הגדרות – 20 נקודות חיים", rtl = true),
        Sample("fa", "Persian", "شروع بازی – تنظیمات – ۲۰ امتیاز", rtl = true),
        Sample("ur", "Urdu", "کھیل شروع کریں – ترتیبات – 20", rtl = true),
        Sample("ka", "Georgian", "თამაშის დაწყება – პარამეტრები"),
    ),
    "indic-a" to listOf(
        Sample("hi", "Hindi", "खेल शुरू करें – सेटिंग्स – क्षत्रिय ज्ञान"),
        Sample("mr", "Marathi", "खेळ सुरू करा – सेटिंग्ज – ळ ऱ्या"),
        Sample("ne", "Nepali", "खेल सुरु गर्नुहोस् – सेटिङहरू"),
        Sample("gu", "Gujarati", "રમત શરૂ કરો – સેટિંગ્સ"),
        Sample("pa", "Punjabi", "ਖੇਡ ਸ਼ੁਰੂ ਕਰੋ – ਸੈਟਿੰਗਾਂ"),
    ),
    "indic-b" to listOf(
        Sample("kn", "Kannada", "ಆಟ ಪ್ರಾರಂಭಿಸಿ – ಸೆಟ್ಟಿಂಗ್‌ಗಳು"),
        Sample("ml", "Malayalam", "ഗെയിം ആരംഭിക്കുക – ക്രമീകരണങ്ങൾ"),
        Sample("ta", "Tamil", "விளையாட்டைத் தொடங்கு – அமைப்புகள்"),
        Sample("te", "Telugu", "ఆట ప్రారంభించండి – సెట్టింగ్‌లు"),
        Sample("si", "Sinhala", "ක්‍රීඩාව අරඹන්න – සැකසීම්"),
    ),
    "sea" to listOf(
        Sample("th", "Thai", "เริ่มเกม – การตั้งค่า – ปิ่นโต ฤๅษี"),
        Sample("lo", "Lao", "ເລີ່ມເກມ – ການຕັ້ງຄ່າ"),
        Sample("km", "Khmer", "ចាប់ផ្ដើមហ្គេម – ការកំណត់"),
        Sample("my", "Burmese", "ဂိမ်းစတင်ပါ – ဆက်တင်များ"),
    ),
    "cjk" to listOf(
        Sample("zh-Hans", "Chinese Simplified", "开始游戏 – 设置 – 生命值"),
        Sample("zh-Hant", "Chinese Traditional", "開始遊戲 – 設定 – 生命值"),
        Sample("ja", "Japanese", "ゲームを開始 – 設定 – ライフ"),
        Sample("ko", "Korean", "게임 시작 – 설정 – 생명력"),
    ),
)
