package dev.dkarski.tcglivecounter.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/** Static facts for Settings > About. */
data class AboutInfo(val appName: String, val version: String, val developer: String)

/**
 * Contact row state. [canOpenEmailApp] decides what tapping the row does: open the e-mail app, or copy the
 * address (the address is always shown, so it can be read either way).
 */
data class ContactState(val email: String, val canOpenEmailApp: Boolean, val copied: Boolean)

/**
 * Settings (content of the Main Layout): Appearance, then About.
 * Row style follows the Kompakt system Settings (bold label, value below, divider) with MMD components.
 * [contact] is null when no developer e-mail is configured.
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    about: AboutInfo,
    contact: ContactState?,
    onContact: () -> Unit,
    onCopyEmail: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SettingsSection(stringResource(R.string.settings_appearance))
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

        // A future Support section goes here, between Appearance and About.

        Spacer(Modifier.height(20.dp))
        SettingsSection(stringResource(R.string.settings_about))
        InfoRow(stringResource(R.string.settings_about_app), about.appName)
        InfoRow(stringResource(R.string.settings_about_version), about.version)
        InfoRow(stringResource(R.string.settings_about_developer), about.developer, divider = contact != null)
        if (contact != null) {
            ContactRow(contact, onClick = if (contact.canOpenEmailApp) onContact else onCopyEmail)
        }
    }
}

/**
 * The whole Contact row is the action (no separate button): it opens the e-mail app when one exists,
 * otherwise it copies the address. The small line under the address says what a tap does.
 */
@Composable
private fun ContactRow(contact: ContactState, onClick: () -> Unit) {
    val hint = when {
        contact.canOpenEmailApp -> R.string.settings_contact_tap_email
        contact.copied -> R.string.coming_soon_copied
        else -> R.string.settings_contact_tap_copy
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button, interactionSource = null, indication = null)
            .padding(vertical = 8.dp),
    ) {
        TextMMD(stringResource(R.string.settings_about_contact), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        TextMMD(contact.email, style = MaterialTheme.typography.bodyLarge)
        TextMMD(stringResource(hint), style = MaterialTheme.typography.labelMedium)
    }
}

/** Section heading, one step above the row labels (like "System" / "Information" in the system Settings). */
@Composable
private fun SettingsSection(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

/** Label (bold) with its value below, then a dotted divider, like the rows of the system Settings. */
@Composable
private fun InfoRow(label: String, value: String, divider: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        TextMMD(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        TextMMD(value, style = MaterialTheme.typography.bodyLarge)
    }
    if (divider) DottedDivider()
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun SettingsPreview() {
    AppTheme(ThemeMode.Light) {
        SettingsScreen(
            themeMode = ThemeMode.Light,
            onThemeModeChange = {},
            about = AboutInfo("Lifepoints", "0.1.0", "Developer Name"),
            contact = ContactState("dev@example.com", canOpenEmailApp = false, copied = false),
            onContact = {},
            onCopyEmail = {},
        )
    }
}
