package dev.dkarski.tcglivecounter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {
    @Test fun appliedChangeIsTheFullDeltaInsideTheLimits() {
        val g = GameState.new(20)
        assertEquals(-10, g.appliedChange(0, -10))
        assertEquals(15, g.withLifeChange(1, -5).lives[1])
    }

    @Test fun appliedChangeStopsAtTheLimits() {
        val low = GameState(20, 2, listOf(GameState.MIN_LIFE + 4, 20))
        assertEquals(-4, low.appliedChange(0, -10))
        assertEquals(GameState.MIN_LIFE, low.withLifeChange(0, -10).lives[0])
        val atMin = low.withLifeChange(0, -10)
        assertEquals(0, atMin.appliedChange(0, -1))
        val high = GameState(20, 2, listOf(20, GameState.MAX_LIFE - 3))
        assertEquals(3, high.appliedChange(1, 10))
    }

    @Test fun lifeMayGoBelowZero() {
        assertEquals(-3, GameState.new(2).withLifeChange(0, -5).lives[0])
    }

    @Test fun progressAndRestart() {
        val g = GameState.new(30)
        assertFalse(g.hasProgress)
        val played = g.withLifeChange(0, -7)
        assertTrue(played.hasProgress)
        assertEquals(GameState.new(30), played.restarted())
    }
}
