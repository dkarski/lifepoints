package dev.dkarski.tcglivecounter.ui

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.components.DottedDivider
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Static facts shown at the top of About & Support. */
data class AboutInfo(val appName: String, val version: String, val developer: String)

/** Contact under "Made by". [canOpenEmailApp] decides what a tap does: open the e-mail app, or copy. */
data class ContactState(val email: String, val canOpenEmailApp: Boolean, val copied: Boolean)

/** An open source part shown in Open source licenses. */
private data class OpenSourcePart(val name: String, val license: String, val copyright: String)

private val OPEN_SOURCE_PARTS = listOf(
    OpenSourcePart("Jetpack Compose, AndroidX", "Apache 2.0", "© The Android Open Source Project"),
    OpenSourcePart("Kotlin, kotlinx.coroutines", "Apache 2.0", "© JetBrains s.r.o."),
    OpenSourcePart("Mudita Mindful Design (MMD)", "Apache 2.0", "© Mudita Sp. z o.o."),
    OpenSourcePart("Lato", "SIL Open Font License 1.1", "© Łukasz Dziedzic"),
)

@Composable
fun AboutScreen(
    about: AboutInfo,
    contact: ContactState?,
    onContact: () -> Unit,
    onCopyEmail: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Header(about, contact, onClick = if (contact?.canOpenEmailApp == true) onContact else onCopyEmail)
            DottedDivider()
            Section(stringResource(R.string.about_privacy)) {
                Paragraph(R.string.privacy_offline_title, stringResource(R.string.privacy_offline))
                Paragraph(R.string.privacy_data_title, stringResource(R.string.privacy_data))
                Paragraph(R.string.privacy_permissions_title, stringResource(R.string.privacy_permissions))
                Paragraph(R.string.privacy_links_title, stringResource(R.string.privacy_links))
            }
            Section(stringResource(R.string.about_terms)) {
                Paragraph(R.string.terms_free_title, stringResource(R.string.terms_free))
                Paragraph(R.string.terms_liability_title, stringResource(R.string.terms_liability, about.developer, contact?.email.orEmpty()))
            }
            Section(stringResource(R.string.about_licenses)) { OpenSourceLicenses() }
            Section(stringResource(R.string.about_legal)) {
                TextMMD(stringResource(R.string.legal_notes), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** App icon, name and version, then "Made by", the author and the tappable contact address. */
@Composable
private fun Header(about: AboutInfo, contact: ContactState?, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The launcher foreground has an 18-unit margin on each side: 84 dp shows the icon at 56 dp.
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(84.dp))
        TextMMD(about.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextMMD(stringResource(R.string.about_version, about.version), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        TextMMD(stringResource(R.string.about_made_by), style = MaterialTheme.typography.bodyMedium)
        TextMMD(about.developer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (contact != null) {
            Column(
                modifier = Modifier.clickable(onClick = onClick, role = Role.Button, interactionSource = null, indication = null),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextMMD(contact.email, style = MaterialTheme.typography.bodyMedium)
                val hint = when {
                    contact.canOpenEmailApp -> R.string.settings_contact_tap_email
                    contact.copied -> R.string.coming_soon_copied
                    else -> R.string.settings_contact_tap_copy
                }
                TextMMD(stringResource(hint), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Collapsible section: title row with a chevron, content below when open, dotted divider. No animation (E Ink). */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { open = !open }, role = Role.Button, interactionSource = null, indication = null)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            painterResource(if (open) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    if (open) {
        Column(Modifier.padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
    DottedDivider()
}

@Composable
private fun Paragraph(titleRes: Int, text: String) {
    Column {
        TextMMD(stringResource(titleRes), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        TextMMD(text, style = MaterialTheme.typography.bodySmall)
    }
}

/** The parts and their licenses; the full license texts open on demand (read from res/raw off the main thread). */
@Composable
private fun OpenSourceLicenses() {
    TextMMD(stringResource(R.string.licenses_intro), style = MaterialTheme.typography.bodySmall)
    OPEN_SOURCE_PARTS.forEach { part ->
        Column {
            TextMMD(part.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            TextMMD("${part.license} · ${part.copyright}", style = MaterialTheme.typography.bodySmall)
        }
    }
    LicenseText("Apache 2.0", R.raw.license_apache_2_0)
    LicenseText("SIL OFL 1.1", R.raw.license_ofl_1_1)
}

@Composable
private fun LicenseText(name: String, rawRes: Int) {
    var open by rememberSaveable(name) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { open = !open }, role = Role.Button, interactionSource = null, indication = null),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextMMD(stringResource(R.string.license_text, name), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(painterResource(if (open) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
    if (open) {
        val context = LocalContext.current
        var text by remember(rawRes) { mutableStateOf("") }
        LaunchedEffect(rawRes) { text = readRaw(context, rawRes) }
        TextMMD(text, style = MaterialTheme.typography.labelSmall)
    }
}

private suspend fun readRaw(context: Context, rawRes: Int): String = withContext(Dispatchers.IO) {
    context.resources.openRawResource(rawRes).bufferedReader().use { it.readText() }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun AboutPreview() {
    AppTheme(ThemeMode.Light) {
        AboutScreen(
            about = AboutInfo("Lifepoints", "1.0.0", "Developer Name"),
            contact = ContactState("dev@example.com", canOpenEmailApp = false, copied = false),
            onContact = {}, onCopyEmail = {},
        )
    }
}
