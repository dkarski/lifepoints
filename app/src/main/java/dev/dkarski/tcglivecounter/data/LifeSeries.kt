package dev.dkarski.tcglivecounter.data

/**
 * The net total of one player's recent life changes, shown above the life total ("+3", "−10").
 * Only changes that were applied count (at a life limit a step may change nothing). It is not saved and is not a
 * game history: a series ends [TIMEOUT_MS] after its last change, or when the counter is left.
 */
data class LifeSeries(val total: Int, val lastChangeMs: Long) {
    fun isActive(nowMs: Long): Boolean = nowMs - lastChangeMs < TIMEOUT_MS

    companion object {
        const val TIMEOUT_MS = 3_000L

        /** [current] with an applied change of [delta], or a new series when [current] has ended. */
        fun record(current: LifeSeries?, delta: Int, nowMs: Long): LifeSeries? = when {
            delta == 0 -> current
            current == null || !current.isActive(nowMs) -> LifeSeries(delta, nowMs)
            else -> LifeSeries(current.total + delta, nowMs)
        }
    }
}
