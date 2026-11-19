package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import dev.dkarski.tcglivecounter.ui.components.TileHeight
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/** Features announced but not built yet. [subject] is the (English, untranslated) feedback e-mail subject suffix. */
enum class Feature(val subject: String) {
    Players3("3 Players"),
    Players4("4 Players"),
    History("History"),
}

/** What the contact section shows. */
enum class FeedbackState {
    /** No developer e-mail configured: no contact action at all. */
    Hidden,
    /** "Contact developer" button. */
    Ready,
    /** No e-mail app on the device: show the address with a copy action. */
    NoEmailApp,
    /** Address copied to the clipboard. */
    Copied,
}

/**
 * Shared Coming Soon screen (content of the Main Layout), the same copy for every [Feature].
 * No icon: MMD v1 ships no icon set and the generic Material icons read too technical here.
 */
@Composable
fun ComingSoonScreen(
    feedback: FeedbackState,
    email: String,
    onContact: () -> Unit,
    onCopyEmail: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextMMD(stringResource(R.string.coming_soon_title), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        TextMMD(
            text = stringResource(R.string.coming_soon_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (feedback != FeedbackState.Hidden) {
            TextMMD(
                text = stringResource(R.string.coming_soon_feedback_prompt),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            ButtonMMD(onClick = onContact, modifier = Modifier.fillMaxWidth().height(TileHeight)) {
                TextMMD(stringResource(R.string.coming_soon_contact), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        // Fallback only after "Contact developer" found no e-mail app, so both actions never compete for space up front.
        if (feedback == FeedbackState.NoEmailApp || feedback == FeedbackState.Copied) {
            Spacer(Modifier.height(4.dp))
            TextMMD(
                text = stringResource(R.string.coming_soon_no_email_app),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            TextMMD(email, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            AppOutlinedButton(onClick = onCopyEmail, modifier = Modifier.fillMaxWidth().height(TileHeight)) {
                TextMMD(
                    text = stringResource(if (feedback == FeedbackState.Copied) R.string.coming_soon_copied else R.string.coming_soon_copy),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun ComingSoonPreview() {
    AppTheme(ThemeMode.Light) { ComingSoonScreen(FeedbackState.NoEmailApp, "dev@example.com", {}, {}) }
}
