package dev.dkarski.tcglivecounter.ui

/** How a watched press ended. */
enum class PressEnd { Released, Cancelled }

/** Time between the ±10 steps while a button is held (E Ink: each value must be readable). */
const val HOLD_REPEAT_MS = 600L

/**
 * One press on a −/+ button.
 * - Released before [holdMs]: a tap, [onTap] runs once.
 * - Still pressed at [holdMs]: a hold, [onStep] runs at once and then every [repeatMs] until the press ends.
 *   The release after a hold does nothing, so a hold never adds a tap.
 * - Cancelled (finger moved off the button, window lost focus): nothing more happens.
 * [waitForEnd] waits up to the given time and returns how the press ended, or null while it is still pressed.
 * Inline, so it runs both inside Compose's restricted pointer scope and in plain coroutine tests.
 */
inline fun handlePress(
    holdMs: Long,
    repeatMs: Long,
    waitForEnd: (timeoutMs: Long) -> PressEnd?,
    onTap: () -> Unit,
    onStep: () -> Unit,
) {
    when (waitForEnd(holdMs)) {
        PressEnd.Released -> onTap()
        PressEnd.Cancelled -> Unit
        null -> do {
            onStep()
        } while (waitForEnd(repeatMs) == null)
    }
}
