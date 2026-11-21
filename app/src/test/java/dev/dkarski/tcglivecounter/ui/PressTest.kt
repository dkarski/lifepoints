package dev.dkarski.tcglivecounter.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** [handlePress] on virtual time: a press that ends after [endAt] ms, as released or cancelled. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PressTest {
    private val hold = 400L

    private class Result(val taps: Int, val steps: List<Long>)

    private suspend fun TestScope.press(endAt: Long, end: PressEnd = PressEnd.Released): Result {
        var taps = 0
        val steps = mutableListOf<Long>()
        handlePress(
            holdMs = hold,
            repeatMs = HOLD_REPEAT_MS,
            waitForEnd = { timeout ->
                val left = endAt - currentTime
                if (left <= timeout) {
                    delay(left)
                    end
                } else {
                    delay(timeout)
                    null
                }
            },
            onTap = { taps++ },
            onStep = { steps += currentTime },
        )
        return Result(taps, steps)
    }

    @Test fun shortPressIsExactlyOneTap() = runTest {
        val r = press(endAt = 120)
        assertEquals(1, r.taps)
        assertEquals(emptyList<Long>(), r.steps)
    }

    @Test fun releaseJustBeforeTheThresholdIsStillATap() = runTest {
        val r = press(endAt = hold - 1)
        assertEquals(1, r.taps)
        assertEquals(0, r.steps.size)
    }

    @Test fun holdOfThreeSecondsGivesFiveStepsAndNoTap() = runTest {
        val r = press(endAt = 3000)
        assertEquals(0, r.taps)
        assertEquals(listOf(400L, 1000L, 1600L, 2200L, 2800L), r.steps)
    }

    @Test fun firstStepComesAtTheThreshold() = runTest {
        val r = press(endAt = hold + 1)
        assertEquals(0, r.taps)
        assertEquals(listOf(hold), r.steps)
    }

    @Test fun cancelledShortPressDoesNothing() = runTest {
        val r = press(endAt = 100, end = PressEnd.Cancelled)
        assertEquals(0, r.taps)
        assertEquals(0, r.steps.size)
    }

    @Test fun cancelledHoldStopsRepeating() = runTest {
        val r = press(endAt = 1200, end = PressEnd.Cancelled)
        assertEquals(0, r.taps)
        assertEquals(listOf(400L, 1000L), r.steps)
    }

    @Test fun formatShowsTheSign() {
        assertEquals("+3", formatChange(3))
        assertEquals("−10", formatChange(-10))
        assertEquals("0", formatChange(0))
        assertEquals("−999", formatLife(-999))
        assertEquals("20", formatLife(20))
    }
}
