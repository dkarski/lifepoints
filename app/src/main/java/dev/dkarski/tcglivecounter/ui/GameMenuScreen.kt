package dev.dkarski.tcglivecounter.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.components.AppOutlinedButton
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

private val MenuButtonHeight = 64.dp

/** Fullscreen Game Menu: the same view on launch (saved game) and from the counter's center button. Two actions, no title. */
@Composable
fun GameMenuScreen(onContinue: () -> Unit, onNewGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // One discreet line that reads naturally both on launch and from the counter's menu button.
        TextMMD(stringResource(R.string.menu_in_progress), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        ButtonMMD(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(MenuButtonHeight)) {
            TextMMD(stringResource(R.string.menu_continue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        AppOutlinedButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth().height(MenuButtonHeight)) {
            TextMMD(stringResource(R.string.menu_new_game), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(widthDp = 320, heightDp = 533)
@Composable
private fun GameMenuPreview() {
    AppTheme(ThemeMode.Light) { GameMenuScreen({}, {}) }
}

@Preview(widthDp = 320, heightDp = 533)
@Composable
private fun GameMenuDarkPreview() {
    AppTheme(ThemeMode.Dark) { GameMenuScreen({}, {}) }
}
