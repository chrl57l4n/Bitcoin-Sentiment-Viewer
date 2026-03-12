package com.bitcoin.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitcoin.dashboard.data.model.*
import com.bitcoin.dashboard.data.repository.BitcoinRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val currentPrice: Double = 0.0,
    val priceChange: Double = 0.0,
    val priceChangePct: Double = 0.0,
    val priceHistory: List<PricePoint> = emptyList(),
    val sentiment: List<SentimentPoint> = emptyList(),
    val exchangeReserve: List<ExchangePoint> = emptyList(),
    val fearGreed: List<FearGreedPoint> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val selectedTimeframe: Timeframe = Timeframe.MONTHS,
    val isLoading: Boolean = false,
    val isPriceLoading: Boolean = false,
    val error: String? = null,
    val isLive: Boolean = true
)

class DashboardViewModel : ViewModel() {

    private val repository = BitcoinRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var livePriceJob: Job? = null
    private var dataLoadJob: Job? = null

    init {
        loadAll()
        startLivePriceUpdates()
    }

    fun setTimeframe(tf: Timeframe) {
        _uiState.value = _uiState.value.copy(selectedTimeframe = tf)
        loadChartData(tf)
    }

    fun refresh() {
        loadAll()
    }

    private fun loadAll() {
        val tf = _uiState.value.selectedTimeframe
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        dataLoadJob?.cancel()
        dataLoadJob = viewModelScope.launch {
            // Parallel loading
            val priceDeferred = async { repository.getKlines(tf.binanceInterval, tf.binanceLimit) }
            val sentDeferred = async { repository.getSentiment(minOf(tf.days, 365)) }
            val exchDeferred = async { repository.getExchangeReserve(minOf(tf.days, 365)) }
            val fgDeferred = async { repository.getFearGreed() }
            val newsDeferred = async { repository.getNews() }

            val priceResult = priceDeferred.await()
            val sentResult = sentDeferred.await()
            val exchResult = exchDeferred.await()
            val fgResult = fgDeferred.await()
            val newsResult = newsDeferred.await()

            val priceHistory = priceResult.getOrElse { emptyList() }
            val change = if (priceHistory.size >= 2) {
                val first = priceHistory.first().price
                val last = priceHistory.last().price
                Pair(last - first, (last - first) / first * 100)
            } else Pair(0.0, 0.0)

            _uiState.value = _uiState.value.copy(
                priceHistory = priceHistory,
                priceChange = change.first,
                priceChangePct = change.second,
                sentiment = sentResult.getOrElse { emptyList() },
                exchangeReserve = exchResult.getOrElse { emptyList() },
                fearGreed = fgResult.getOrElse { emptyList() },
                news = newsResult.getOrElse { emptyList() },
                isLoading = false,
                error = priceResult.exceptionOrNull()?.message
            )
        }
    }

    private fun loadChartData(tf: Timeframe) {
        dataLoadJob?.cancel()
        dataLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPriceLoading = true)

            val priceDeferred = async { repository.getKlines(tf.binanceInterval, tf.binanceLimit) }
            val sentDeferred = async { repository.getSentiment(minOf(tf.days, 365)) }
            val exchDeferred = async { repository.getExchangeReserve(minOf(tf.days, 365)) }

            val priceHistory = priceDeferred.await().getOrElse { emptyList() }
            val change = if (priceHistory.size >= 2) {
                val first = priceHistory.first().price
                val last = priceHistory.last().price
                Pair(last - first, (last - first) / first * 100)
            } else Pair(0.0, 0.0)

            _uiState.value = _uiState.value.copy(
                priceHistory = priceHistory,
                priceChange = change.first,
                priceChangePct = change.second,
                sentiment = sentDeferred.await().getOrElse { emptyList() },
                exchangeReserve = exchDeferred.await().getOrElse { emptyList() },
                isPriceLoading = false
            )
        }
    }

    private fun startLivePriceUpdates() {
        livePriceJob = viewModelScope.launch {
            while (isActive) {
                repository.getLivePrice().onSuccess { price ->
                    _uiState.value = _uiState.value.copy(currentPrice = price)
                }
                delay(10_000L) // alle 10 Sekunden
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        livePriceJob?.cancel()
        dataLoadJob?.cancel()
    }
}
