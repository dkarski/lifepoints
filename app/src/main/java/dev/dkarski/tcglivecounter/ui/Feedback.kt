package dev.dkarski.tcglivecounter.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.dkarski.tcglivecounter.AppConfig
import dev.dkarski.tcglivecounter.R

/** True when some app can send e-mail (needs the mailto <queries> entry in the manifest on Android 11+). */
fun hasEmailApp(context: Context): Boolean =
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).resolveActivity(context.packageManager) != null

/** Feedback about a Coming Soon feature: subject "Lifepoints — History" etc. */
fun openFeedbackEmail(context: Context, feature: Feature): Boolean = openContactEmail(context, feature.subject)

/**
 * Opens the user's e-mail app with recipient, subject ("Lifepoints — [subjectSuffix]") and an editable body.
 * Nothing is sent automatically. Returns false when no app can handle e-mail.
 */
fun openContactEmail(context: Context, subjectSuffix: String): Boolean {
    val email = AppConfig.DEVELOPER_EMAIL
    val subject = "${AppConfig.FEEDBACK_SUBJECT_PREFIX} — $subjectSuffix"
    val body = context.getString(R.string.feedback_body)
    val uri = Uri.parse("mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

/** Copies the developer address to the clipboard (no permission needed). */
fun copyDeveloperEmail(context: Context) {
    context.getSystemService(ClipboardManager::class.java)
        ?.setPrimaryClip(ClipData.newPlainText("email", AppConfig.DEVELOPER_EMAIL))
}
