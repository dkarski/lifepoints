package dev.dkarski.tcglivecounter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import dev.dkarski.tcglivecounter.ui.components.SecondaryLayout

/**
 * The counter is the main screen. Its center button opens the Menu (Back to game, Restart, Settings).
 * Settings has the table (starting life, players) and the app settings; Language, About, TipJar and
 * ComingSoon (3/4 players) open from it.
 */
enum class Screen { Counter, Menu, Settings, ComingSoon, Language, About, TipJar }

/** Where Back goes from each screen; null = system Back (leaves the app; the game is already saved). */
private fun Screen.parent(): Screen? = when (this) {
    Screen.Counter -> null
    Screen.Menu -> Screen.Counter
    Screen.Settings -> Screen.Menu
    Screen.ComingSoon, Screen.Language, Screen.About, Screen.TipJar -> Screen.Settings
}

/**
 * Root composable: navigation, Back handling and the active game.
 * [initialGame] is loaded (or created) by the Activity; every change is passed to [onGameChange] to be saved.
 */
@Composable
fun App(
    initialGame: GameState,
    onGameChange: (GameState) -> Unit,
    languageTag: String?,
    onLanguageChange: (String?) -> Unit,
    vibration: Boolean,
    onVibrationChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var game by remember { mutableStateOf(initialGame) }
    fun setGame(state: GameState) {
        game = state
        onGameChange(state)
    }
    var screen by rememberSaveable { mutableStateOf(Screen.Counter) }
    // Keeps each screen's own UI state (scroll position, open sections) while another screen is shown,
    // so Back returns to the screen exactly as it was.
    val screenStates = rememberSaveableStateHolder()

    // Restart asks first only when the game has progress (life totals differ from the start).
    var confirmingRestart by rememberSaveable { mutableStateOf(false) }
    // The same confirmation, opened by Settings' Restart for a pending table change: Cancel returns to Settings.
    var confirmingFromSettings by rememberSaveable { mutableStateOf(false) }
    // Table change waiting for confirmation (only while the game has progress); null = no pending change.
    // Dropped when Settings is left towards the Menu.
    var pendingLife by rememberSaveable { mutableStateOf<Int?>(null) }

    // Coming soon (3/4 players) and contact.
    var feature by rememberSaveable { mutableStateOf(Feature.Players3) }
    val feedbackEnabled = AppConfig.DEVELOPER_EMAIL.isNotBlank()
    var feedback by rememberSaveable { mutableStateOf(FeedbackState.Ready) }
    fun showComingSoon(f: Feature) {
        feature = f
        feedback = if (feedbackEnabled) FeedbackState.Ready else FeedbackState.Hidden
        screen = Screen.ComingSoon
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

    fun cancelRestart() {
        confirmingRestart = false
        if (confirmingFromSettings) {
            confirmingFromSettings = false
            screen = Screen.Settings // the pending table change is kept
        }
    }

    val back: () -> Unit = {
        when {
            screen == Screen.Menu && confirmingRestart -> cancelRestart()
            else -> screen.parent()?.let { parent ->
                if (screen == Screen.Settings) pendingLife = null
                screen = parent
            }
        }
    }
    BackHandler(enabled = screen.parent() != null, onBack = back)

    /** Restarts with the pending table, if Settings has one, otherwise with the same table. */
    fun restart() {
        setGame(pendingLife?.let { GameState.new(it, game.playerCount) } ?: game.restarted())
        pendingLife = null
        confirmingRestart = false
        confirmingFromSettings = false
        screen = Screen.Counter
    }

    // Surface provides the theme's content colour (onBackground) to every screen, also the fullscreen ones.
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        screenStates.SaveableStateProvider(screen.name) {
            when (screen) {
                Screen.Counter -> GameCounterScreen(
                    lives = game.lives,
                    onLifeChange = { player, delta ->
                        // At a life limit nothing changes: nothing to save, no indicator step, no vibration.
                        game.appliedChange(player, delta).also { applied ->
                            if (applied != 0) setGame(game.withLifeChange(player, delta))
                        }
                    },
                    onMenu = { screen = Screen.Menu },
                    vibration = vibration,
                )

                Screen.Menu -> GameMenuScreen(
                    confirmingRestart = confirmingRestart,
                    startingLife = (if (confirmingFromSettings) pendingLife else null) ?: game.startingLife,
                    onResume = { screen = Screen.Counter },
                    onRestart = { if (game.hasProgress) confirmingRestart = true else restart() },
                    onConfirmRestart = ::restart,
                    onCancelRestart = ::cancelRestart,
                    onSettings = { screen = Screen.Settings },
                )

                Screen.Settings -> SecondaryLayout(title = stringResource(R.string.settings_title), onBack = back) { padding ->
                    SettingsScreen(
                        tableLife = pendingLife ?: game.startingLife,
                        onTableLifeSelect = { life ->
                            when {
                                // Nothing to lose: the new table starts at once.
                                !game.hasProgress -> {
                                    if (life != game.startingLife) setGame(GameState.new(life, game.playerCount))
                                    pendingLife = null
                                }
                                life == game.startingLife -> pendingLife = null
                                else -> pendingLife = life
                            }
                        },
                        onComingSoonPlayers = ::showComingSoon,
                        pendingLife = pendingLife,
                        // Restart in Settings opens the same confirmation as the menu, with the new starting life.
                        onApplyTable = {
                            confirmingFromSettings = true
                            confirmingRestart = true
                            screen = Screen.Menu
                        },
                        vibration = vibration,
                        onVibrationChange = onVibrationChange,
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

                Screen.ComingSoon -> SecondaryLayout(
                    title = stringResource(if (feature == Feature.Players4) R.string.feature_players_4 else R.string.feature_players_3),
                    onBack = back,
                ) { padding ->
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
            }
        }
    }
}
