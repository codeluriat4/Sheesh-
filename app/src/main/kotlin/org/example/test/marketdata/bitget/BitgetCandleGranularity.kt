package org.example.test.marketdata.bitget

import org.example.test.domain.chart.Timeframe

// Bitget's wire name for each candle granularity, paired with the
// Timeframe it maps from. Kept as one lookup table rather than a branch
// over Timeframe instances, so adding a granularity is adding a row here,
// not touching every call site that already maps one.
data class BitgetCandleGranularity(val timeframe: Timeframe, val wireValue: String) {

    companion object {
        private val ALL: List<BitgetCandleGranularity> = listOf(
            BitgetCandleGranularity(Timeframe.OneMinute, "candle1m"),
            BitgetCandleGranularity(Timeframe.FiveMinutes, "candle5m"),
            BitgetCandleGranularity(Timeframe.FifteenMinutes, "candle15m"),
            BitgetCandleGranularity(Timeframe.ThirtyMinutes, "candle30m"),
            BitgetCandleGranularity(Timeframe.OneHour, "candle1H"),
            BitgetCandleGranularity(Timeframe.FourHours, "candle4H"),
            BitgetCandleGranularity(Timeframe.OneDay, "candle1D"),
            BitgetCandleGranularity(Timeframe.OneWeek, "candle1W"),
        )

        private val byTimeframe: Map<Timeframe, BitgetCandleGranularity> = ALL.associateBy { it.timeframe }
        private val byWireValue: Map<String, BitgetCandleGranularity> = ALL.associateBy { it.wireValue }

        fun of(timeframe: Timeframe): BitgetCandleGranularity = byTimeframe.getValue(timeframe)

        fun timeframeOf(wireValue: String): Timeframe? = byWireValue[wireValue]?.timeframe
    }
}
