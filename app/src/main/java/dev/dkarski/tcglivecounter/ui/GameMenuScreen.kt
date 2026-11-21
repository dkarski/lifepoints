package dev.dkarski.tcglivecounter.ui

import androidx.annotation.StringRes
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.components.AppOutlinedButton
import dev.dkarski.tcglivecounter.ui.theme.AppTheme

private val MenuButtonHeight = 64.dp
private val ButtonGap = 16.dp

/** Space above the buttons: the app icon at its top; the restart question grows upwards into the rest. */
private val HeaderHeight = 112.dp

/** Visible size of the app icon (the launcher artwork without its transparent margin). */
private val IconSize = 80.dp

/**
 * Fullscreen Game Menu, opened from the counter's center button: Back to game, Restart, Settings, under the app icon.
 * While [confirmingRestart] the same screen asks to confirm (no dialog: one static redraw on E Ink).
 * The icon and the buttons sit in fixed slots, so confirming never moves them: the confirming Restart is exactly
 * where Restart was, Cancel takes the place of Settings, and the question takes the place of Back to game.
 */
@Composable
fun GameMenuScreen(
    confirmingRestart: Boolean,
    startingLife: Int,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onConfirmRestart: () -> Unit,
    onCancelRestart: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.fillMaxWidth().height(HeaderHeight), contentAlignment = Alignment.TopCenter) {
            // The launcher artwork: pure black and white, no background picture. Decorative for screen readers.
            // Drawn larger than its box: the adaptive-icon margin (a third of the canvas) falls outside.
            Box(Modifier.size(IconSize), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(IconSize * 108 / 72),
                )
            }
        }
        Spacer(Modifier.height(ButtonGap))
        // Slot 1: Back to game, or the restart question aligned to its bottom and growing upwards.
        Box(Modifier.fillMaxWidth().height(MenuButtonHeight), contentAlignment = Alignment.BottomCenter) {
            if (confirmingRestart) {
                Column(
                    modifier = Modifier.wrapContentHeight(align = Alignment.Bottom, unbounded = true),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextMMD(
                        stringResource(R.string.restart_question),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextMMD(
                        stringResource(R.string.restart_body, startingLife),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                MenuButton(R.string.menu_continue, filled = true, onClick = onResume)
            }
        }
        Spacer(Modifier.height(ButtonGap))
        // Slot 2: Restart, in the same place before and while confirming.
        if (confirmingRestart) {
            MenuButton(R.string.menu_restart, filled = true, onClick = onConfirmRestart)
        } else {
            MenuButton(R.string.menu_restart, filled = false, onClick = onRestart)
        }
        Spacer(Modifier.height(ButtonGap))
        // Slot 3: Settings, or Cancel while confirming.
        if (confirmingRestart) {
            MenuButton(R.string.cancel, filled = false, onClick = onCancelRestart)
        } else {
            MenuButton(R.string.settings_title, filled = false, onClick = onSettings)
        }
        // Balances the header, so the three buttons stay in the middle of the screen.
        Spacer(Modifier.height(HeaderHeight + ButtonGap))
    }
}

@Composable
private fun MenuButton(@StringRes text: Int, filled: Boolean, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().height(MenuButtonHeight)
    val label: @Composable () -> Unit = {
        TextMMD(stringResource(text), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    if (filled) ButtonMMD(onClick = onClick, modifier = modifier) { label() } else AppOutlinedButton(onClick = onClick, modifier = modifier) { label() }
}

@Preview(widthDp = 320, heightDp = 533)
@Composable
private fun GameMenuPreview() {
    AppTheme { GameMenuScreen(false, 20, {}, {}, {}, {}, {}) }
}

@Preview(widthDp = 320, heightDp = 533)
@Composable
private fun GameMenuConfirmPreview() {
    AppTheme { GameMenuScreen(true, 20, {}, {}, {}, {}, {}) }
}
