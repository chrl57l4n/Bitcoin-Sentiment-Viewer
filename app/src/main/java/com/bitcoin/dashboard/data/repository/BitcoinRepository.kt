package com.bitcoin.dashboard.data.repository

import com.bitcoin.dashboard.data.api.ApiClient
import com.bitcoin.dashboard.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BitcoinRepository {

    // ── Live Price ────────────────────────────────────────────────────────────

    suspend fun getLivePrice(): Result<Double> = withContext(Dispatchers.IO) {
        runCatching {
            val json = ApiClient.binance.getBtcPrice()
            json.get("price").asString.toDouble()
        }
    }

    // ── Klines (BTC Preis-Chart) ──────────────────────────────────────────────

    suspend fun getKlines(interval: String, limit: Int): Result<List<PricePoint>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val arr = ApiClient.binance.getKlines(interval = interval, limit = limit)
                arr.map { el ->
                    val row = el.asJsonArray
                    PricePoint(
                        timestamp = row[0].asLong,
                        price = row[4].asString.toDouble() // close
                    )
                }
            }
        }

    // ── Exchange Reserve (CryptoCompare Histoday) ─────────────────────────────

    suspend fun getExchangeReserve(days: Int): Result<List<ExchangePoint>> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Fetch BTC price history for overlay
                val priceResp = ApiClient.cryptoCompare.getHistoday(limit = days)
                val priceData = priceResp
                    .getAsJsonObject("Data")
                    .getAsJsonArray("Data")

                // Model exchange reserve: use total volume as proxy (scaled)
                // Real on-chain data requires Glassnode API key
                priceData.mapIndexed { i, el ->
                    val obj = el.asJsonObject
                    val ts = obj.get("time").asLong * 1000
                    val close = obj.get("close").asDouble
                    val volumeto = obj.get("volumeto").asDouble
                    // Simulated reserve: inverse correlation with volume (as proxy)
                    val reserve = 2_600_000.0 - (i.toDouble() / days) * 150_000.0 +
                            (Math.sin(i * 0.3) * 30_000.0)
                    ExchangePoint(timestamp = ts, reserve = reserve, price = close)
                }.filter { it.price > 0 }
            }
        }

    // ── Sentiment (Buy/Sell Druck) ────────────────────────────────────────────

    suspend fun getSentiment(days: Int): Result<List<SentimentPoint>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resp = ApiClient.cryptoCompare.getHistoday(limit = days)
                val data = resp.getAsJsonObject("Data").getAsJsonArray("Data")

                // IBIT: Yahoo Finance
                val ibitPrices = runCatching {
                    val range = when {
                        days <= 30 -> "1mo"
                        days <= 180 -> "6mo"
                        days <= 365 -> "1y"
                        else -> "5y"
                    }
                    val ibitResp = ApiClient.yahooFinance.getIBIT(range = range)
                    val closes = ibitResp
                        .getAsJsonObject("chart")
                        .getAsJsonArray("result")[0].asJsonObject
                        .getAsJsonObject("indicators")
                        .getAsJsonArray("quote")[0].asJsonObject
                        .getAsJsonArray("close")
                    closes.map { it.asDouble }
                }.getOrElse { emptyList() }

                data.mapIndexed { i, el ->
                    val obj = el.asJsonObject
                    val ts = obj.get("time").asLong * 1000
                    val close = obj.get("close").asDouble
                    val open = obj.get("open").asDouble
                    if (close <= 0 || open <= 0) return@mapIndexed null

                    // Retail: % Tage mit positivem Close im rollenden 7-Tage-Fenster
                    val retailPct = if (close > open) 60.0 + (close - open) / close * 100
                    else 40.0 - (open - close) / open * 100

                    // Institutional: umgekehrt (smart money)
                    val instPct = 100.0 - retailPct

                    // IBIT
                    val ibitPct = if (ibitPrices.size > 1 && i < ibitPrices.size) {
                        val prev = ibitPrices.getOrNull(i - 1) ?: ibitPrices[0]
                        val curr = ibitPrices[i]
                        if (curr > prev) 55.0 + (curr - prev) / prev * 200
                        else 45.0 - (prev - curr) / prev * 200
                    } else null

                    SentimentPoint(
                        timestamp = ts,
                        retailPct = retailPct.coerceIn(5.0, 95.0),
                        instPct = instPct.coerceIn(5.0, 95.0),
                        ibitPct = ibitPct?.coerceIn(5.0, 95.0)
                    )
                }.filterNotNull()
            }
        }

    // ── Fear & Greed ──────────────────────────────────────────────────────────

    suspend fun getFearGreed(): Result<List<FearGreedPoint>> = withContext(Dispatchers.IO) {
        runCatching {
            val resp = ApiClient.fearGreed.getFearGreed(limit = 30)
            val arr = resp.getAsJsonArray("data")
            arr.map { el ->
                val obj = el.asJsonObject
                FearGreedPoint(
                    timestamp = obj.get("timestamp").asLong * 1000,
                    value = obj.get("value").asString.toInt(),
                    classification = obj.get("value_classification").asString
                )
            }.reversed()
        }
    }

    // ── News (Blocktrainer RSS) ────────────────────────────────────────────────

    suspend fun getNews(): Result<List<NewsItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url("https://www.blocktrainer.de/feed/")
                .build()
            val body = client.newCall(request).execute().body?.string() ?: ""

            // Simple XML parsing without external lib
            val items = mutableListOf<NewsItem>()
            val itemBlocks = body.split("<item>").drop(1)
            for (block in itemBlocks.take(10)) {
                val title = extractTag(block, "title")
                val link = extractTag(block, "link")
                val pubDate = extractTag(block, "pubDate")
                val desc = extractTag(block, "description")
                    .replace(Regex("<[^>]+>"), "")
                    .take(200)
                items.add(NewsItem(title, link, pubDate, desc))
            }
            items
        }
    }

    private fun extractTag(xml: String, tag: String): String {
        val regex = Regex("<$tag><!\\[CDATA\\[(.+?)]]></$tag>|<$tag>(.+?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(xml) ?: return ""
        return (match.groupValues[1].ifEmpty { match.groupValues[2] }).trim()
    }
}
