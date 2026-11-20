package dev.dkarski.tcglivecounter.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.components.ChoiceTile
import dev.dkarski.tcglivecounter.ui.components.DottedDivider
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    languageName: String,
    onLanguage: () -> Unit,
    onAbout: () -> Unit,
    onTipJar: (() -> Unit)?,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SettingsSection(stringResource(R.string.settings_theme))
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
        Spacer(Modifier.height(24.dp))
        DottedDivider()
        SettingsRow(stringResource(R.string.settings_language), onClick = onLanguage, value = languageName)
        DottedDivider()
        SettingsRow(stringResource(R.string.settings_about), onClick = onAbout)
        DottedDivider()
        if (onTipJar != null) {
            SettingsRow(stringResource(R.string.about_tip_jar), onClick = onTipJar)
            DottedDivider()
        }
    }
}

/** Section heading (like "System" / "Information" in the system Settings). */
@Composable
internal fun SettingsSection(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
internal fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    @DrawableRes chevron: Int = R.drawable.ic_chevron_right,
    value: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button, interactionSource = null, indication = null)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value != null) {
            // Current value (e.g. the chosen language), like the system Settings rows.
            TextMMD(
                value,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp).widthIn(max = 170.dp),
            )
        }
        Icon(painterResource(chevron), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun SettingsPreview() {
    AppTheme(ThemeMode.Light) { SettingsScreen(ThemeMode.Light, {}, "English", {}, {}, {}) }
}
