package org.example.test.domain.chart

// One selectable candle duration. Each case owns its own duration and
// label, so adding a timeframe never touches a branch elsewhere.
sealed class Timeframe(val label: String, val durationMillis: Long) {
    data object OneMinute : Timeframe("1m", 60_000L)
    data object FiveMinutes : Timeframe("5m", 5 * 60_000L)
    data object FifteenMinutes : Timeframe("15m", 15 * 60_000L)
    data object ThirtyMinutes : Timeframe("30m", 30 * 60_000L)
    data object OneHour : Timeframe("1h", 60 * 60_000L)
    data object FourHours : Timeframe("4h", 4 * 60 * 60_000L)
    data object OneDay : Timeframe("1D", 24 * 60 * 60_000L)
    data object OneWeek : Timeframe("1W", 7 * 24 * 60 * 60_000L)

    // Which open-time bucket a timestamp falls into on this timeframe.
    fun bucketStartMillis(timestampMillis: Long): Long =
        timestampMillis - (timestampMillis % durationMillis)

    companion object {
        // Lazy so this list is built on first access rather than during
        // Timeframe's own <clinit>. Eagerly initializing it there let the
        // JVM's class-init reentrancy rules hand back a partially-built
        // Timeframe (some data object fields still null) whenever something
        // touched ALL (directly or via a default parameter) before the
        // outer class had finished initializing its object declarations.
        val ALL: List<Timeframe> by lazy {
            listOf(OneMinute, FiveMinutes, FifteenMinutes, ThirtyMinutes, OneHour, FourHours, OneDay, OneWeek)
        }

        // The curated subset a compact selector control offers, as opposed
        // to a full menu. Kept here, alongside ALL, so the set of quick
        // options is defined once and every consumer stays in sync.
        val QUICK_SELECT: List<Timeframe> by lazy {
            listOf(OneMinute, FiveMinutes, FifteenMinutes, ThirtyMinutes, OneHour)
        }
    }
}
