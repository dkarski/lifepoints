package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.components.ChoiceTile
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/** Settings (content of the Main Layout). Support / About / Contact arrive in iteration 2. */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionLabel(stringResource(R.string.settings_theme))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceTile(
                label = stringResource(R.string.settings_theme_light),
                selected = themeMode == ThemeMode.Light,
                onClick = { onThemeModeChange(ThemeMode.Light) },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                label = stringResource(R.string.settings_theme_dark),
                selected = themeMode == ThemeMode.Dark,
                onClick = { onThemeModeChange(ThemeMode.Dark) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun SettingsPreview() {
    AppTheme(ThemeMode.Light) { SettingsScreen(ThemeMode.Light, {}) }
}
