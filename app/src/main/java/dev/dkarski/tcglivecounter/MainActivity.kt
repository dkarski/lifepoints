package dev.dkarski.tcglivecounter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import dev.dkarski.tcglivecounter.data.AppLocale
import dev.dkarski.tcglivecounter.data.GameState
import dev.dkarski.tcglivecounter.data.GameStore
import dev.dkarski.tcglivecounter.data.SettingsStore
import dev.dkarski.tcglivecounter.ui.App
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.AppColors

class MainActivity : ComponentActivity() {
    /** Applies the app language chosen in Settings (or keeps the system language). */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameStore = GameStore(this)
        val settingsStore = SettingsStore(this)
        // Two tiny preference files, read once at start so the first frame already has the right screen.
        // The app opens on the table. First launch (or unreadable data): two players at the default starting life.
        val initialGame = gameStore.load() ?: GameState.new(GameState.DEFAULT_STARTING_LIFE).also(gameStore::save)
        applySystemBars()

        setContent {
            var vibration by remember { mutableStateOf(settingsStore.vibration) }
            AppTheme {
                App(
                    initialGame = initialGame,
                    onGameChange = gameStore::save,
                    languageTag = settingsStore.languageTag,
                    vibration = vibration,
                    onVibrationChange = { on ->
                        vibration = on
                        settingsStore.vibration = on
                    },
                    onLanguageChange = { tag ->
                        if (tag != settingsStore.languageTag) {
                            settingsStore.languageTag = tag
                            // Rebuilds the screen with the new resources. The game is already saved and the
                            // navigation state survives (rememberSaveable), so nothing is lost.
                            recreate()
                        }
                    },
                )
            }
        }
    }

    /** Window background and system bars in the app's colours: white with dark icons. */
    @Suppress("DEPRECATION") // statusBarColor/navigationBarColor are still the API on Android 12 (Kompakt).
    private fun applySystemBars() {
        val color = AppColors.background.toArgb()
        window.setBackgroundDrawable(ColorDrawable(color))
        window.statusBarColor = color
        window.navigationBarColor = color
        val lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // window.insetsController is null-unsafe before the decor view exists (NPE on Android 12);
        // decorView creates it on first access.
        window.decorView.windowInsetsController?.setSystemBarsAppearance(lightBars, lightBars)
    }
}
