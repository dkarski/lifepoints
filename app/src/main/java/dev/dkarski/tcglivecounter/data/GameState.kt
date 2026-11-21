package dev.dkarski.tcglivecounter.data

/**
 * The single active game. [lives] has one entry per player (index 0 = Player 1), so 3 and 4 players
 * need no model change later.
 */
data class GameState(
    val startingLife: Int,
    val playerCount: Int,
    val lives: List<Int>,
) {
    fun withLifeChange(player: Int, delta: Int): GameState =
        copy(lives = lives.mapIndexed { i, life -> if (i == player) (life + delta).coerceIn(MIN_LIFE, MAX_LIFE) else life })

    /** The change of [player]'s life that [withLifeChange] would apply: less than [delta] at a life limit. */
    fun appliedChange(player: Int, delta: Int): Int =
        (lives[player] + delta).coerceIn(MIN_LIFE, MAX_LIFE) - lives[player]

    /** True when a life total differs from the starting life, so restarting would lose something. */
    val hasProgress: Boolean get() = lives.any { it != startingLife }

    /** The same table (starting life, players) with every life back at the start. */
    fun restarted(): GameState = new(startingLife, playerCount)

    companion object {
        /** Life may go below zero; the bounds only keep the number at four characters on screen. */
        const val MIN_LIFE = -999
        const val MAX_LIFE = 9999
        const val MIN_STARTING_LIFE = 1
        const val MAX_STARTING_LIFE = 9999
        val PRESETS = listOf(20, 30, 40)
        const val DEFAULT_STARTING_LIFE = 20
        const val DEFAULT_PLAYER_COUNT = 2

        fun new(startingLife: Int, playerCount: Int = DEFAULT_PLAYER_COUNT) =
            GameState(startingLife, playerCount, List(playerCount) { startingLife })
    }
}
