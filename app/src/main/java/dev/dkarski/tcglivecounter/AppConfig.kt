package dev.dkarski.tcglivecounter

/** Central app configuration. Contact details live only here. */
object AppConfig {
    /** Developer contact for feedback. Leave empty to hide every feedback action in the app. */
    const val DEVELOPER_EMAIL = "daniel.karski5q@gmail.com"

    /** Shown in Settings > About. */
    const val DEVELOPER_NAME = "Daniel Karski"

    /** Subject suffix of the general contact e-mail from Settings ("Lifepoints — Feedback"). */
    const val CONTACT_SUBJECT = "Feedback"

    /** Prefix of feedback e-mail subjects, e.g. "Lifepoints — History". */
    const val FEEDBACK_SUBJECT_PREFIX = "Lifepoints"
}
