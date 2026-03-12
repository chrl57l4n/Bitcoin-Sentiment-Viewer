package com.bitcoin.dashboard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitcoin.dashboard.ui.components.*
import com.bitcoin.dashboard.ui.theme.*
import com.bitcoin.dashboard.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    val priceFormatted = remember(state.currentPrice) {
        if (state.currentPrice > 0)
            "$${NumberFormat.getNumberInstance(Locale.US).format(state.currentPrice.toLong())}" +
            ".${String.format("%02d", ((state.currentPrice % 1) * 100).toInt())}"
        else "Lädt..."
    }

    val changeColor = when {
        state.priceChangePct >= 0 -> NeonGreen
        else -> NeonRed
    }

    val sentimentAvgRetail = state.sentiment.mapNotNull { it.retailPct }.average().takeIf { it.isFinite() }
    val sentimentAvgInst = state.sentiment.mapNotNull { it.instPct }.average().takeIf { it.isFinite() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Bitcoin Logo
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(BtcOrange, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("₿", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Column {
                        Text("Bitcoin", fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = TextPrimary, letterSpacing = (-0.5).sp)
                        Text("BTC / USD · BINANCE", fontSize = 11.sp, color = TextSecondary,
                            letterSpacing = 1.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BtcOrange, strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { vm.refresh() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren",
                                tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    LiveBadge()
                }
            }
        }

        // ── Preis ─────────────────────────────────────────────────────────────
        item {
            Column {
                Text(priceFormatted, fontSize = 42.sp, fontWeight = FontWeight.Black,
                    color = TextPrimary, letterSpacing = (-1.5).sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(changeColor.copy(alpha = 0.15f),
                                androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "${if (state.priceChange >= 0) "+" else ""}$${
                                String.format("%,.2f", abs(state.priceChange)).replace(",", ".")
                            }",
                            fontSize = 12.sp, color = changeColor, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(changeColor.copy(alpha = 0.15f),
                                androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "${if (state.priceChangePct >= 0) "+" else ""}${
                                String.format("%.2f", state.priceChangePct)
                            }%",
                            fontSize = 12.sp, color = changeColor, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "${state.selectedTimeframe.label}-VERLAUF",
                        fontSize = 11.sp, color = TextSecondary
                    )
                }
            }
        }

        // ── Timeframe Buttons ─────────────────────────────────────────────────
        item {
            TimeframeButtons(selected = state.selectedTimeframe, onSelect = { vm.setTimeframe(it) })
        }

        // ── BTC Preis Chart ───────────────────────────────────────────────────
        item {
            DashCard(modifier = Modifier.fillMaxWidth()) {
                if (state.isPriceLoading) {
                    Box(Modifier.fillMaxWidth().height(260.dp), Alignment.Center) {
                        CircularProgressIndicator(color = BtcOrange)
                    }
                } else if (state.priceHistory.isNotEmpty()) {
                    PriceChart(
                        data = state.priceHistory,
                        timeframe = state.selectedTimeframe,
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                } else {
                    Box(Modifier.fillMaxWidth().height(260.dp), Alignment.Center) {
                        Text("Keine Daten", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Marktsentiment ────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "MARKTSENTIMENT",
                    legend = listOf(
                        NeonRed to "Retail (< 1 BTC)",
                        TextPrimary to "Institutionell (≥ 1 BTC)",
                        BtcOrange to "BlackRock IBIT"
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SentimentGauge(
                        label = "RETAIL · < 1 BTC KAUFDRUCK",
                        sublabel = "retail",
                        value = sentimentAvgRetail,
                        modifier = Modifier.weight(1f)
                    )
                    SentimentGauge(
                        label = "INSTITUTIONELL · ≥ 1 BTC KAUFDRUCK",
                        sublabel = "inst",
                        value = sentimentAvgInst,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (state.sentiment.isNotEmpty()) {
                    DashCard(modifier = Modifier.fillMaxWidth()) {
                        SentimentChart(
                            data = state.sentiment,
                            timeframe = state.selectedTimeframe,
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        )
                    }
                }
            }
        }

        // ── BTC auf Börsen ────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "BTC AUF BÖRSEN",
                    legend = listOf(
                        BtcOrange to "Reserve",
                        NeonBlue to "BTC Preis"
                    )
                )
                DashCard(modifier = Modifier.fillMaxWidth()) {
                    if (state.exchangeReserve.isNotEmpty()) {
                        ExchangeChart(
                            data = state.exchangeReserve,
                            timeframe = state.selectedTimeframe,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                            Text("Lädt...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── Fear & Greed ──────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FEAR & GREED INDEX", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = TextSecondary, letterSpacing = 2.sp)
                    state.fearGreed.lastOrNull()?.let {
                        FearGreedBadge(it.value, it.classification)
                    }
                }
                DashCard(modifier = Modifier.fillMaxWidth()) {
                    if (state.fearGreed.isNotEmpty()) {
                        FearGreedChart(
                            data = state.fearGreed,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(150.dp), Alignment.Center) {
                            Text("Lädt...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── News ──────────────────────────────────────────────────────────────
        if (state.news.isNotEmpty()) {
            item {
                Text("NACHRICHTEN · BLOCKTRAINER", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = TextSecondary, letterSpacing = 2.sp)
            }
            items(state.news) { newsItem ->
                NewsCard(newsItem)
            }
        }

        // ── Fehler ────────────────────────────────────────────────────────────
        state.error?.let { err ->
            item {
                Text("⚠ $err", color = NeonRed, fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
