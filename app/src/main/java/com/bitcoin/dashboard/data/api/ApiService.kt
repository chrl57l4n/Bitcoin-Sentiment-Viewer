package com.bitcoin.dashboard.data.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ── Binance ──────────────────────────────────────────────────────────────────

interface BinanceApi {
    @GET("api/v3/ticker/price")
    suspend fun getBtcPrice(@Query("symbol") symbol: String = "BTCUSDT"): JsonObject

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String = "BTCUSDT",
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): JsonArray
}

// ── CryptoCompare ─────────────────────────────────────────────────────────────

interface CryptoCompareApi {
    @GET("data/v2/histoday")
    suspend fun getHistoday(
        @Query("fsym") from: String = "BTC",
        @Query("tsym") to: String = "USD",
        @Query("limit") limit: Int = 30
    ): JsonObject

    @GET("data/exchange/histoday")
    suspend fun getExchangeHistoday(
        @Query("tsym") tsym: String = "USD",
        @Query("limit") limit: Int = 30,
        @Query("e") exchange: String = "Binance"
    ): JsonObject
}

// ── Alternative.me Fear & Greed ───────────────────────────────────────────────

interface FearGreedApi {
    @GET("fng/")
    suspend fun getFearGreed(@Query("limit") limit: Int = 30): JsonObject
}

// ── Yahoo Finance (IBIT) ──────────────────────────────────────────────────────

interface YahooFinanceApi {
    @GET("v8/finance/chart/IBIT")
    suspend fun getIBIT(
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "1mo"
    ): JsonObject
}

// ── Retrofit Instances ────────────────────────────────────────────────────────

object ApiClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    val binance: BinanceApi = Retrofit.Builder()
        .baseUrl("https://api.binance.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BinanceApi::class.java)

    val cryptoCompare: CryptoCompareApi = Retrofit.Builder()
        .baseUrl("https://min-api.cryptocompare.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CryptoCompareApi::class.java)

    val fearGreed: FearGreedApi = Retrofit.Builder()
        .baseUrl("https://api.alternative.me/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FearGreedApi::class.java)

    val yahooFinance: YahooFinanceApi = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YahooFinanceApi::class.java)
}
