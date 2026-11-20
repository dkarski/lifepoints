package dev.dkarski.tcglivecounter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.dkarski.tcglivecounter.AppConfig
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.data.AppLanguages
import dev.dkarski.tcglivecounter.data.GameState
import dev.dkarski.tcglivecounter.ui.components.MainLayout
import dev.dkarski.tcglivecounter.ui.components.SecondaryLayout
import dev.dkarski.tcglivecounter.ui.components.Tab
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/**
 * Main screens (bottom navigation, no back arrow): Home (Game tab), History, Settings.
 * Secondary screens (back arrow, no bottom navigation): ComingSoon (3/4 players, from Home), Language, About and
 * TipJar (from Settings). Fullscreen: Counter, Menu.
 */
enum class Screen { Home, History, Settings, ComingSoon, Language, About, TipJar, Counter, Menu }

/** Where Back goes from each screen; null = system Back (leaves the app; the game is already saved). */
private fun Screen.parent(): Screen? = when (this) {
    Screen.Home, Screen.Menu -> null
    Screen.History, Screen.Settings, Screen.ComingSoon -> Screen.Home
    Screen.Language, Screen.About, Screen.TipJar -> Screen.Settings
    Screen.Counter -> Screen.Menu
}

/**
 * Root composable: navigation, Back handling and the active game.
 * [initialGame] is loaded by the Activity; every change is passed to [onGameChange] to be saved.
 */
@Composable
fun App(
    initialGame: GameState?,
    onGameChange: (GameState) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    languageTag: String?,
    onLanguageChange: (String?) -> Unit,
) {
    val context = LocalContext.current
    var game by remember { mutableStateOf(initialGame) }
    var screen by rememberSaveable { mutableStateOf(if (initialGame != null) Screen.Menu else Screen.Home) }
    // Keeps each screen's own UI state (scroll position, open sections) while another screen is shown,
    // so Back returns to the screen exactly as it was.
    val screenStates = rememberSaveableStateHolder()

    // New Game draft. lifePreset == null means Custom (customText applies).
    var lifePreset by rememberSaveable { mutableStateOf<Int?>(GameState.DEFAULT_STARTING_LIFE) }
    var customText by rememberSaveable { mutableStateOf("") }
    fun resetDraftFrom(state: GameState?) {
        val life = state?.startingLife ?: GameState.DEFAULT_STARTING_LIFE
        if (life in GameState.PRESETS) {
            lifePreset = life
            customText = ""
        } else {
            lifePreset = null
            customText = life.toString()
        }
    }
    val draftLife: Int? = lifePreset
        ?: customText.toIntOrNull()?.takeIf { it in GameState.MIN_STARTING_LIFE..GameState.MAX_STARTING_LIFE }

    // Coming soon (History tab, or 3/4 players from Home) and contact.
    var feature by rememberSaveable { mutableStateOf(Feature.History) }
    val feedbackEnabled = AppConfig.DEVELOPER_EMAIL.isNotBlank()
    var feedback by rememberSaveable { mutableStateOf(FeedbackState.Ready) }
    fun showComingSoon(f: Feature) {
        feature = f
        feedback = if (feedbackEnabled) FeedbackState.Ready else FeedbackState.Hidden
        screen = if (f == Feature.History) Screen.History else Screen.ComingSoon
    }
    // About & Support. Version comes from the installed package (versionName in app/build.gradle.kts).
    val about = remember {
        AboutInfo(
            appName = context.getString(R.string.app_name),
            version = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty(),
            developer = AppConfig.DEVELOPER_NAME,
        )
    }
    var emailAppAvailable by remember { mutableStateOf(hasEmailApp(context)) }
    var contactCopied by rememberSaveable { mutableStateOf(false) }

    val back: () -> Unit = { screen.parent()?.let { screen = it } }
    BackHandler(enabled = screen.parent() != null, onBack = back)

    val onTabSelected: (Tab) -> Unit = { tab ->
        when (tab) {
            Tab.Game -> screen = Screen.Home
            Tab.History -> showComingSoon(Feature.History)
            Tab.Settings -> screen = Screen.Settings
        }
    }

    val comingSoon: @Composable (PaddingValues) -> Unit = { padding ->
        ComingSoonScreen(
            feedback = feedback,
            email = AppConfig.DEVELOPER_EMAIL,
            onContact = {
                if (!openFeedbackEmail(context, feature)) feedback = FeedbackState.NoEmailApp
            },
            onCopyEmail = {
                copyDeveloperEmail(context)
                feedback = FeedbackState.Copied
            },
            contentPadding = padding,
        )
    }

    // Surface provides the theme's content colour (onBackground) to every screen, also the fullscreen ones.
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        screenStates.SaveableStateProvider(screen.name) {
            when (screen) {
                Screen.Home -> MainLayout(selectedTab = Tab.Game, onTabSelected = onTabSelected) { padding ->
                    HomeScreen(
                        lifePreset = lifePreset,
                        customText = customText,
                        canStart = draftLife != null,
                        onPresetSelect = { lifePreset = it },
                        onCustomSelect = { lifePreset = null },
                        onCustomTextChange = { customText = it },
                        onComingSoonPlayers = ::showComingSoon,
                        onStart = {
                            val life = draftLife ?: return@HomeScreen
                            val newGame = GameState.new(life)
                            game = newGame
                            onGameChange(newGame)
                            screen = Screen.Counter
                        },
                        contentPadding = padding,
                    )
                }

                Screen.History -> MainLayout(selectedTab = Tab.History, onTabSelected = onTabSelected, content = comingSoon)

                Screen.ComingSoon -> SecondaryLayout(
                    title = stringResource(if (feature == Feature.Players4) R.string.feature_players_4 else R.string.feature_players_3),
                    onBack = back,
                    content = comingSoon,
                )

                Screen.Settings -> MainLayout(selectedTab = Tab.Settings, onTabSelected = onTabSelected) { padding ->
                    SettingsScreen(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        languageName = AppLanguages.find(languageTag)?.nativeName
                            ?: stringResource(R.string.language_system_default),
                        onLanguage = { screen = Screen.Language },
                        onAbout = {
                            contactCopied = false
                            screen = Screen.About
                        },
                        onTipJar = if (TipJarQr.URL.isNotBlank()) { { screen = Screen.TipJar } } else null,
                        contentPadding = padding,
                    )
                }

                Screen.Language -> LanguageScreen(
                    selectedTag = languageTag,
                    onSelect = { tag ->
                        // Back to Settings first: the Activity is rebuilt in the new language and restores this screen.
                        screen = Screen.Settings
                        onLanguageChange(tag)
                    },
                    onBack = back,
                )

                Screen.About -> SecondaryLayout(title = stringResource(R.string.about_title), onBack = back) { padding ->
                    AboutScreen(
                        about = about,
                        contact = if (feedbackEnabled) {
                            ContactState(AppConfig.DEVELOPER_EMAIL, canOpenEmailApp = emailAppAvailable, copied = contactCopied)
                        } else {
                            null
                        },
                        onContact = {
                            // The check can be stale (e-mail app removed meanwhile): fall back to copying.
                            if (!openContactEmail(context, AppConfig.CONTACT_SUBJECT)) emailAppAvailable = false
                        },
                        onCopyEmail = {
                            copyDeveloperEmail(context)
                            contactCopied = true
                        },
                        contentPadding = padding,
                    )
                }

                Screen.TipJar -> SecondaryLayout(title = stringResource(R.string.about_tip_jar), onBack = back) { padding ->
                    TipJarScreen(url = TipJarQr.URL, contentPadding = padding)
                }

                Screen.Counter, Screen.Menu -> {
                    val current = game
                    if (current == null) {
                        // Defensive: the fullscreen views need a game. Fall back to New Game.
                        LaunchedEffect(Unit) { screen = Screen.Home }
                    } else if (screen == Screen.Counter) {
                        GameCounterScreen(
                            lives = current.lives,
                            onLifeChange = { player, delta ->
                                val updated = current.withLifeChange(player, delta)
                                game = updated
                                onGameChange(updated)
                            },
                            onMenu = { screen = Screen.Menu },
                        )
                    } else {
                        GameMenuScreen(
                            onContinue = { screen = Screen.Counter },
                            onNewGame = {
                                // The saved game stays until Start Game replaces it.
                                resetDraftFrom(current)
                                screen = Screen.Home
                            },
                        )
                    }
                }
            }
        }
    }
}
