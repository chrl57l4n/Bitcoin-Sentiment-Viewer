package com.bitcoin.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitcoin.dashboard.data.model.*
import com.bitcoin.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val PAD_LEFT = 12.dp
private val PAD_RIGHT = 70.dp
private val PAD_TOP = 24.dp
private val PAD_BOTTOM = 36.dp

// ── BTC Preis Chart ──────────────────────────────────────────────────────────

@Composable
fun PriceChart(
    data: List<PricePoint>,
    timeframe: Timeframe,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Normal)

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val padLeft = PAD_LEFT.toPx()
        val padRight = PAD_RIGHT.toPx()
        val padTop = PAD_TOP.toPx()
        val padBottom = PAD_BOTTOM.toPx()
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom

        val prices = data.map { it.price }
        val minP = prices.min()
        val maxP = prices.max()
        val range = (maxP - minP).coerceAtLeast(1.0)

        fun xOf(i: Int) = padLeft + (i.toFloat() / (data.size - 1)) * chartW
        fun yOf(p: Double) = padTop + chartH - ((p - minP) / range * chartH).toFloat()

        // Grid lines
        repeat(4) { i ->
            val y = padTop + (i.toFloat() / 3) * chartH
            drawLine(GridLine, Offset(padLeft, y), Offset(padLeft + chartW, y), strokeWidth = 0.5.dp.toPx())
        }

        // Y-axis labels
        repeat(4) { i ->
            val price = maxP - i * range / 3
            val y = padTop + (i.toFloat() / 3) * chartH
            val label = "$${formatPrice(price)}"
            val measured = textMeasurer.measure(label, axisStyle)
            drawText(measured, topLeft = Offset(size.width - padRight + 4.dp.toPx(), y - measured.size.height / 2))
        }

        // Gradient fill
        val path = Path().apply {
            moveTo(xOf(0), yOf(prices[0]))
            data.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOf(p.price)) }
            lineTo(xOf(data.size - 1), padTop + chartH)
            lineTo(xOf(0), padTop + chartH)
            close()
        }
        drawPath(path, brush = Brush.verticalGradient(
            colors = listOf(NeonRed.copy(alpha = 0.25f), Color.Transparent),
            startY = padTop, endY = padTop + chartH
        ))

        // Price line
        val linePath = Path().apply {
            moveTo(xOf(0), yOf(prices[0]))
            data.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOf(p.price)) }
        }
        drawPath(linePath, color = NeonRed, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        // Endpoint dot with glow
        val ex = xOf(data.size - 1)
        val ey = yOf(prices.last())
        drawCircle(NeonRed.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(ex, ey))
        drawCircle(NeonRed, radius = 3.5.dp.toPx(), center = Offset(ex, ey))

        // X-axis labels
        drawXAxisLabels(data.map { it.timestamp }, timeframe, textMeasurer, axisStyle,
            padLeft, padTop + chartH + 6.dp.toPx(), chartW)
    }
}

// ── Fear & Greed Chart ────────────────────────────────────────────────────────

@Composable
fun FearGreedChart(
    data: List<FearGreedPoint>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontSize = 10.sp, color = TextSecondary)
    val barLabelStyle = TextStyle(fontSize = 8.sp, color = TextSecondary)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val padLeft = PAD_LEFT.toPx()
        val padRight = 10.dp.toPx()
        val padTop = PAD_TOP.toPx()
        val padBottom = PAD_BOTTOM.toPx()
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom

        val barW = (chartW / data.size * 0.7f).coerceAtLeast(4f)
        val gap = chartW / data.size

        data.forEachIndexed { i, pt ->
            val x = padLeft + i * gap + gap / 2
            val barH = (pt.value / 100.0 * chartH).toFloat()
            val y = padTop + chartH - barH

            val color = when {
                pt.value < 25 -> Color(0xFFEF4444)
                pt.value < 45 -> Color(0xFFF97316)
                pt.value < 55 -> Color(0xFFEAB308)
                pt.value < 75 -> Color(0xFF84CC16)
                else -> Color(0xFF22C55E)
            }

            drawRect(color.copy(alpha = 0.8f), topLeft = Offset(x - barW / 2, y),
                size = androidx.compose.ui.geometry.Size(barW, barH))
        }

        // X-axis: 5 gleichmäßige Punkte
        val indices = listOf(0, data.size / 4, data.size / 2, 3 * data.size / 4, data.size - 1)
        indices.forEach { i ->
            if (i >= data.size) return@forEach
            val x = padLeft + i * gap + gap / 2
            val label = formatAxisDate(data[i].timestamp, Timeframe.DAYS)
            val measured = textMeasurer.measure(label, axisStyle)
            drawText(measured, topLeft = Offset(x - measured.size.width / 2, padTop + chartH + 6.dp.toPx()))
        }
    }
}

// ── Exchange Reserve Chart (Dual Y-Axis) ─────────────────────────────────────

@Composable
fun ExchangeChart(
    data: List<ExchangePoint>,
    timeframe: Timeframe,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyleOrange = TextStyle(fontSize = 9.sp, color = BtcOrange)
    val axisStyleBlue = TextStyle(fontSize = 9.sp, color = NeonBlue)
    val axisStyle = TextStyle(fontSize = 10.sp, color = TextSecondary)

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val padLeft = 52.dp.toPx()
        val padRight = 64.dp.toPx()
        val padTop = PAD_TOP.toPx()
        val padBottom = PAD_BOTTOM.toPx()
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom

        val reserves = data.map { it.reserve }
        val prices = data.map { it.price }
        val minR = reserves.min(); val maxR = reserves.max()
        val minP = prices.min(); val maxP = prices.max()
        val rangeR = (maxR - minR).coerceAtLeast(1.0)
        val rangeP = (maxP - minP).coerceAtLeast(1.0)

        fun xOf(i: Int) = padLeft + (i.toFloat() / (data.size - 1)) * chartW
        fun yOfR(v: Double) = padTop + chartH - ((v - minR) / rangeR * chartH).toFloat()
        fun yOfP(v: Double) = padTop + chartH - ((v - minP) / rangeP * chartH).toFloat()

        // Grid
        repeat(4) { i ->
            val y = padTop + (i.toFloat() / 3) * chartH
            drawLine(GridLine, Offset(padLeft, y), Offset(padLeft + chartW, y), strokeWidth = 0.5.dp.toPx())
        }

        // Y-Achse links: Reserve (Orange)
        repeat(4) { i ->
            val v = minR + (3 - i) * rangeR / 3
            val y = padTop + (i.toFloat() / 3) * chartH
            val label = "${(v / 1_000).toInt()}K"
            val m = textMeasurer.measure(label, axisStyleOrange)
            drawText(m, topLeft = Offset(2.dp.toPx(), y - m.size.height / 2))
        }

        // Y-Achse rechts: Preis (Blau)
        repeat(4) { i ->
            val v = maxP - i * rangeP / 3
            val y = padTop + (i.toFloat() / 3) * chartH
            val label = "$${formatPrice(v)}"
            val m = textMeasurer.measure(label, axisStyleBlue)
            drawText(m, topLeft = Offset(size.width - padRight + 4.dp.toPx(), y - m.size.height / 2))
        }

        // Reserve-Gradient
        val reservePath = Path().apply {
            moveTo(xOf(0), yOfR(reserves[0]))
            data.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOfR(p.reserve)) }
            lineTo(xOf(data.size - 1), padTop + chartH)
            lineTo(xOf(0), padTop + chartH)
            close()
        }
        drawPath(reservePath, brush = Brush.verticalGradient(
            colors = listOf(BtcOrange.copy(alpha = 0.2f), Color.Transparent),
            startY = padTop, endY = padTop + chartH
        ))

        // Reserve-Linie (Orange)
        val reserveLine = Path().apply {
            moveTo(xOf(0), yOfR(reserves[0]))
            data.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOfR(p.reserve)) }
        }
        drawPath(reserveLine, color = BtcOrange, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        // Preis-Linie (Blau)
        val priceLine = Path().apply {
            moveTo(xOf(0), yOfP(prices[0]))
            data.forEachIndexed { i, p -> if (i > 0) lineTo(xOf(i), yOfP(p.price)) }
        }
        drawPath(priceLine, color = NeonBlue.copy(alpha = 0.7f),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))

        // X-Axis
        drawXAxisLabels(data.map { it.timestamp }, timeframe, textMeasurer, axisStyle,
            padLeft, padTop + chartH + 6.dp.toPx(), chartW)
    }
}

// ── Sentiment Bar Chart ───────────────────────────────────────────────────────

@Composable
fun SentimentChart(
    data: List<SentimentPoint>,
    timeframe: Timeframe,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontSize = 10.sp, color = TextSecondary)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val padLeft = PAD_LEFT.toPx()
        val padRight = 10.dp.toPx()
        val padTop = PAD_TOP.toPx()
        val padBottom = PAD_BOTTOM.toPx()
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom

        val barW = (chartW / data.size * 0.6f).coerceAtLeast(2f)
        val gap = chartW / data.size

        data.forEachIndexed { i, pt ->
            val x = padLeft + i * gap + gap / 2

            // Retail (orange) – unten
            pt.retailPct?.let { rp ->
                val h = (rp / 100.0 * chartH).toFloat()
                val color = if (rp >= 50) NeonGreen.copy(alpha = 0.7f) else NeonRed.copy(alpha = 0.7f)
                drawRect(color, topLeft = Offset(x - barW / 2, padTop + chartH - h),
                    size = androidx.compose.ui.geometry.Size(barW, h))
            }
        }

        // X-Achse
        drawXAxisLabels(data.map { it.timestamp }, timeframe, textMeasurer, axisStyle,
            padLeft, padTop + chartH + 6.dp.toPx(), chartW)
    }
}

// ── Hilfsfunktionen ───────────────────────────────────────────────────────────

private fun DrawScope.drawXAxisLabels(
    timestamps: List<Long>,
    timeframe: Timeframe,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    style: TextStyle,
    startX: Float,
    labelY: Float,
    chartW: Float
) {
    if (timestamps.isEmpty()) return
    val n = timestamps.size

    val indices = when (timeframe) {
        Timeframe.HOURS -> (0..5).map { i -> (i * (n - 1) / 5) }
        Timeframe.DAYS -> (0..4).map { i -> (i * (n - 1) / 4) }
        Timeframe.MONTHS -> (0..5).map { i -> (i * (n - 1) / 5) }
        Timeframe.WEEKS -> (0..5).map { i -> (i * (n - 1) / 5) }
        Timeframe.YEARS -> (0..4).map { i -> (i * (n - 1) / 4) }
    }

    var lastX = -999f
    val minSpacing = chartW / 8

    indices.forEach { i ->
        if (i >= n) return@forEach
        val x = startX + (i.toFloat() / (n - 1)) * chartW
        if (x - lastX < minSpacing) return@forEach
        lastX = x
        val label = formatAxisDate(timestamps[i], timeframe)
        val m = textMeasurer.measure(label, style)
        drawText(m, topLeft = Offset(x - m.size.width / 2, labelY))
    }
}

private fun formatAxisDate(ts: Long, timeframe: Timeframe): String {
    val d = Date(ts)
    return when (timeframe) {
        Timeframe.HOURS -> SimpleDateFormat("HH:mm", Locale.GERMANY).format(d)
        else -> {
            val mm = SimpleDateFormat("MM", Locale.GERMANY).format(d)
            val dd = SimpleDateFormat("dd", Locale.GERMANY).format(d)
            val yy = SimpleDateFormat("yy", Locale.GERMANY).format(d)
            "$mm.$dd.$yy"
        }
    }
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 100_000 -> "${(price / 1000).toInt()}.${((price % 1000) / 100).toInt()}K"
        price >= 10_000 -> String.format("%,.0f", price)
        else -> String.format("%,.2f", price)
    }.replace(",", ".")
}
