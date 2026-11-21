package dev.dkarski.tcglivecounter.ui

import androidx.annotation.StringRes
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
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

private val MenuButtonHeight = 64.dp

/**
 * Fullscreen Game Menu, opened from the counter's center button: Back to game, Restart, Settings.
 * While [confirmingRestart] the same screen asks to confirm (no dialog: one static redraw on E Ink).
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
        if (confirmingRestart) {
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
            Spacer(Modifier.height(24.dp))
            MenuButton(R.string.menu_restart, filled = true, onClick = onConfirmRestart)
            Spacer(Modifier.height(16.dp))
            MenuButton(R.string.cancel, filled = false, onClick = onCancelRestart)
        } else {
            MenuButton(R.string.menu_continue, filled = true, onClick = onResume)
            Spacer(Modifier.height(16.dp))
            MenuButton(R.string.menu_restart, filled = false, onClick = onRestart)
            Spacer(Modifier.height(16.dp))
            MenuButton(R.string.settings_title, filled = false, onClick = onSettings)
        }
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
    AppTheme(ThemeMode.Light) { GameMenuScreen(false, 20, {}, {}, {}, {}, {}) }
}

@Preview(widthDp = 320, heightDp = 533)
@Composable
private fun GameMenuConfirmDarkPreview() {
    AppTheme(ThemeMode.Dark) { GameMenuScreen(true, 20, {}, {}, {}, {}, {}) }
}
