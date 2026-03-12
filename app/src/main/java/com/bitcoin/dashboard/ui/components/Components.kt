package com.bitcoin.dashboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitcoin.dashboard.data.model.*
import com.bitcoin.dashboard.ui.theme.*

// ── Dashboard Card ────────────────────────────────────────────────────────────

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(12.dp))
            .border(0.5.dp, BgCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

// ── LIVE Badge ────────────────────────────────────────────────────────────────

@Composable
fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(
        modifier = Modifier
            .background(BgCard, RoundedCornerShape(20.dp))
            .border(0.5.dp, BgCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(LiveGreen.copy(alpha = alpha))
        )
        Text("LIVE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = TextPrimary, letterSpacing = 1.sp)
    }
}

// ── Timeframe Buttons ─────────────────────────────────────────────────────────

@Composable
fun TimeframeButtons(
    selected: Timeframe,
    onSelect: (Timeframe) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Timeframe.entries.forEach { tf ->
            val isSelected = tf == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) BtcOrange else BgCard,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        0.5.dp,
                        if (isSelected) BtcOrange else BgCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(tf) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = tf.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ── Sentiment Gauge ───────────────────────────────────────────────────────────

@Composable
fun SentimentGauge(
    label: String,
    sublabel: String,
    value: Double?,
    modifier: Modifier = Modifier
) {
    DashCard(modifier = modifier) {
        Text(
            label, fontSize = 10.sp, color = TextSecondary,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        if (value != null) {
            val isUp = value >= 50
            val color = if (isUp) NeonGreen else NeonRed
            Text(
                "${String.format("%.1f", value)}%",
                fontSize = 32.sp, fontWeight = FontWeight.Black, color = color
            )
            Text(
                "${if (isUp) "↑" else "↓"} ${if (isUp) "KAUFT" else "VERKAUFT"}",
                fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(TextMuted, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value.toFloat() / 100f)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
        } else {
            Text("Lädt...", fontSize = 20.sp, color = TextSecondary)
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    legend: List<Pair<Color, String>> = emptyList()
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = TextSecondary, letterSpacing = 2.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            legend.forEach { (color, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                    Text(label, fontSize = 9.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ── News Card ─────────────────────────────────────────────────────────────────

@Composable
fun NewsCard(item: com.bitcoin.dashboard.data.model.NewsItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(10.dp))
            .border(0.5.dp, BgCardBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(
            item.title,
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        if (item.pubDate.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(item.pubDate.take(22), fontSize = 10.sp, color = TextSecondary)
        }
        if (item.description.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                item.description, fontSize = 11.sp, color = TextSecondary,
                maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp
            )
        }
    }
}

// ── Current Fear & Greed Index Badge ─────────────────────────────────────────

@Composable
fun FearGreedBadge(value: Int, classification: String) {
    val color = when {
        value < 25 -> Color(0xFFEF4444)
        value < 45 -> Color(0xFFF97316)
        value < 55 -> Color(0xFFEAB308)
        value < 75 -> Color(0xFF84CC16)
        else -> Color(0xFF22C55E)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("$value", fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        Text(
            classification.uppercase(),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            color = color, letterSpacing = 1.sp
        )
    }
}
