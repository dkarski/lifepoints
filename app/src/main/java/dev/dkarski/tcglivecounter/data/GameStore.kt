package dev.dkarski.tcglivecounter.data

import android.content.Context

/** Persists the one active game in SharedPreferences (framework API, no extra dependency). */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)

    fun load(): GameState? {
        val lives = prefs.getString(KEY_LIVES, null)
            ?.split(',')
            ?.mapNotNull { it.toIntOrNull() }
            ?: return null
        val playerCount = prefs.getInt(KEY_PLAYER_COUNT, 0)
        if (playerCount < 1 || lives.size != playerCount) return null
        return GameState(
            startingLife = prefs.getInt(KEY_STARTING_LIFE, GameState.DEFAULT_STARTING_LIFE),
            playerCount = playerCount,
            lives = lives,
        )
    }

    /** Asynchronous write; Android flushes pending writes before the Activity stops. */
    fun save(state: GameState) {
        prefs.edit()
            .putInt(KEY_STARTING_LIFE, state.startingLife)
            .putInt(KEY_PLAYER_COUNT, state.playerCount)
            .putString(KEY_LIVES, state.lives.joinToString(","))
            .apply()
    }

    private companion object {
        const val KEY_STARTING_LIFE = "starting_life"
        const val KEY_PLAYER_COUNT = "player_count"
        const val KEY_LIVES = "lives"
    }
}
