package dev.dkarski.tcglivecounter.ui

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Touch feedback for life changes through the view's haptic feedback: no permission needed, and the phone's
 * touch vibration setting is respected (off there means no vibration here). The system maps the constants to the
 * device's effects; on the Kompakt a tap is a light click and a hold step a heavy click.
 */
enum class Haptic(val constant: Int) {
    /** A ±1 tap. */
    Tap(HapticFeedbackConstants.KEYBOARD_TAP),

    /** Each ±10 step while a button is held: stronger than a tap. */
    Step(HapticFeedbackConstants.LONG_PRESS),
}

fun View.vibrate(haptic: Haptic) {
    performHapticFeedback(haptic.constant)
}
