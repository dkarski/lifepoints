package dev.dkarski.tcglivecounter.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.dkarski.tcglivecounter.AppConfig
import dev.dkarski.tcglivecounter.R

/**
 * Opens the user's e-mail app with recipient, subject and an editable body filled in.
 * Nothing is sent automatically. Returns false when no app can handle e-mail.
 */
fun openFeedbackEmail(context: Context, feature: Feature): Boolean {
    val email = AppConfig.DEVELOPER_EMAIL
    val subject = "${AppConfig.FEEDBACK_SUBJECT_PREFIX} — ${feature.subject}"
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
fun copyFeedbackEmail(context: Context) {
    context.getSystemService(ClipboardManager::class.java)
        ?.setPrimaryClip(ClipData.newPlainText("email", AppConfig.DEVELOPER_EMAIL))
}
