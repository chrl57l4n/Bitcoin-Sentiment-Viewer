package com.bitcoin.dashboard.data.model

data class PricePoint(
    val timestamp: Long,
    val price: Double
)

data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class SentimentPoint(
    val timestamp: Long,
    val retailPct: Double?,
    val instPct: Double?,
    val ibitPct: Double?
)

data class ExchangePoint(
    val timestamp: Long,
    val reserve: Double,
    val price: Double
)

data class FearGreedPoint(
    val timestamp: Long,
    val value: Int,
    val classification: String
)

data class NewsItem(
    val title: String,
    val link: String,
    val pubDate: String,
    val description: String
)

enum class Timeframe(val label: String, val days: Int, val binanceInterval: String, val binanceLimit: Int) {
    HOURS("STUNDEN", 1, "1h", 24),
    DAYS("TAGE", 30, "1d", 30),
    MONTHS("MONATE", 180, "1d", 180),
    WEEKS("WOCHEN", 365, "1w", 52),
    YEARS("JAHRE", 1825, "1M", 60)
}
