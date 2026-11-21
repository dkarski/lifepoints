package dev.dkarski.tcglivecounter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LifeSeriesTest {
    private val t = LifeSeries.TIMEOUT_MS

    @Test fun firstChangeStartsASeries() {
        assertEquals(LifeSeries(-3, 100), LifeSeries.record(null, -3, 100))
    }

    @Test fun changesWithinTheTimeoutAddUp() {
        var s = LifeSeries.record(null, 1, 0)
        s = LifeSeries.record(s, 1, 1000)
        s = LifeSeries.record(s, 1, 1000 + t - 1)
        assertEquals(3, s!!.total)
    }

    @Test fun oppositeDirectionGivesTheNetTotal() {
        val s = LifeSeries.record(LifeSeries.record(null, 3, 0), -1, 500)
        assertEquals(2, s!!.total)
    }

    @Test fun netTotalCanBeZero() {
        val s = LifeSeries.record(LifeSeries.record(null, 10, 0), -10, 500)
        assertEquals(0, s!!.total)
    }

    @Test fun aChangeAfterTheTimeoutStartsANewSeries() {
        val s = LifeSeries.record(LifeSeries.record(null, 5, 0), -1, t)
        assertEquals(LifeSeries(-1, t), s)
    }

    @Test fun noAppliedChangeKeepsTheSeriesAndItsTimer() {
        val s = LifeSeries(4, 200)
        assertEquals(s, LifeSeries.record(s, 0, 1000))
        assertNull(LifeSeries.record(null, 0, 1000))
    }

    @Test fun holdStepsAndTapsShareTheSeries() {
        var s = LifeSeries.record(null, 1, 0)
        s = LifeSeries.record(s, 1, 300)
        s = LifeSeries.record(s, 1, 600)
        listOf(1000L, 1600L).forEach { s = LifeSeries.record(s, 10, it) }
        assertEquals(23, s!!.total)
    }
}
