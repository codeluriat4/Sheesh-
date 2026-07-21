package org.example.test.domain.chart

// The user's current timeframe choice, plus the menu of options it was
// made from. Keeps "what's selected" and "what can be selected" together
// so a screen never has to reconcile them separately. Every transition
// returns a new instance rather than mutating in place.
data class TimeframeSelection(
    val selected: Timeframe,
    val available: List<Timeframe> = Timeframe.ALL,
) {
    init {
        require(available.isNotEmpty()) { "available cannot be empty" }
        require(selected in available) { "selected timeframe must be one of available" }
    }

    fun select(timeframe: Timeframe): TimeframeSelection {
        require(timeframe in available) { "$timeframe is not an available timeframe" }
        return copy(selected = timeframe)
    }

    fun next(): TimeframeSelection = select(available[(available.indexOf(selected) + 1) % available.size])

    fun previous(): TimeframeSelection =
        select(available[(available.indexOf(selected) - 1 + available.size) % available.size])

    companion object {
        fun default(): TimeframeSelection = TimeframeSelection(selected = Timeframe.OneMinute)
    }
}
