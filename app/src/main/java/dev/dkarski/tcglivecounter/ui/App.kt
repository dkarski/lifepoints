package dev.dkarski.tcglivecounter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.dkarski.tcglivecounter.AppConfig
import dev.dkarski.tcglivecounter.data.GameState
import dev.dkarski.tcglivecounter.ui.components.MainLayout
import dev.dkarski.tcglivecounter.ui.components.Tab
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/** Main Layout: Home, Settings, ComingSoon. Fullscreen Layout: Counter, Menu. */
enum class Screen { Home, Settings, ComingSoon, Counter, Menu }

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
) {
    val context = LocalContext.current
    var game by remember { mutableStateOf(initialGame) }
    var screen by rememberSaveable { mutableStateOf(if (initialGame != null) Screen.Menu else Screen.Home) }

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

    // Coming Soon.
    var feature by rememberSaveable { mutableStateOf(Feature.History) }
    var comingSoonReturn by rememberSaveable { mutableStateOf(Screen.Home) }
    val feedbackEnabled = AppConfig.DEVELOPER_EMAIL.isNotBlank()
    var feedback by rememberSaveable { mutableStateOf(FeedbackState.Ready) }
    fun openComingSoon(f: Feature) {
        if (screen != Screen.ComingSoon) comingSoonReturn = screen
        feature = f
        feedback = if (feedbackEnabled) FeedbackState.Ready else FeedbackState.Hidden
        screen = Screen.ComingSoon
    }

    when (screen) {
        Screen.Counter -> BackHandler { screen = Screen.Menu }
        Screen.ComingSoon -> BackHandler { screen = comingSoonReturn }
        Screen.Settings -> BackHandler { screen = Screen.Home }
        Screen.Home, Screen.Menu -> Unit // System Back closes the app; the game is already saved.
    }

    val onTabSelected: (Tab) -> Unit = { tab ->
        when (tab) {
            Tab.Game -> screen = Screen.Home
            Tab.History -> openComingSoon(Feature.History)
            Tab.Settings -> screen = Screen.Settings
        }
    }

    // Surface provides the theme's content colour (onBackground) to every screen, also the fullscreen ones.
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (screen) {
            Screen.Home -> MainLayout(
                selectedTab = Tab.Game,
                onTabSelected = onTabSelected,
            ) { padding ->
                HomeScreen(
                    lifePreset = lifePreset,
                    customText = customText,
                    canStart = draftLife != null,
                    onPresetSelect = { lifePreset = it },
                    onCustomSelect = { lifePreset = null },
                    onCustomTextChange = { customText = it },
                    onComingSoonPlayers = ::openComingSoon,
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

            Screen.Settings -> MainLayout(
                selectedTab = Tab.Settings,
                onTabSelected = onTabSelected,
            ) { padding ->
                SettingsScreen(themeMode = themeMode, onThemeModeChange = onThemeModeChange, contentPadding = padding)
            }

            Screen.ComingSoon -> MainLayout(
                selectedTab = if (feature == Feature.History) Tab.History else Tab.Game,
                onTabSelected = onTabSelected,
            ) { padding ->
                ComingSoonScreen(
                    feedback = feedback,
                    email = AppConfig.DEVELOPER_EMAIL,
                    onContact = {
                        if (!openFeedbackEmail(context, feature)) feedback = FeedbackState.NoEmailApp
                    },
                    onCopyEmail = {
                        copyFeedbackEmail(context)
                        feedback = FeedbackState.Copied
                    },
                    contentPadding = padding,
                )
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
