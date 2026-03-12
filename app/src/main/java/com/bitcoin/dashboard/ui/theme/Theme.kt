package com.bitcoin.dashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Bitcoin Neon Palette ──────────────────────────────────────────────────────

val BtcOrange        = Color(0xFFF7931A)
val BtcOrangeGlow    = Color(0x40F7931A)
val NeonRed          = Color(0xFFFF4466)
val NeonRedGlow      = Color(0x33FF4466)
val NeonGreen        = Color(0xFF00FF88)
val NeonBlue         = Color(0xFF4488FF)
val NeonBlueGlow     = Color(0x334488FF)

val BgDeep           = Color(0xFF0D0D12)
val BgCard           = Color(0xFF14141C)
val BgCardBorder     = Color(0xFF1E1E2E)
val TextPrimary      = Color(0xFFE8E8F0)
val TextSecondary    = Color(0xFF46566E)
val TextMuted        = Color(0xFF2A3A52)

val GridLine         = Color(0x1A46566E)
val LiveGreen        = Color(0xFF22C55E)

private val DarkColorScheme = darkColorScheme(
    primary = BtcOrange,
    secondary = NeonBlue,
    background = BgDeep,
    surface = BgCard,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BgCardBorder
)

@Composable
fun BitcoinDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-1).sp
            ),
            titleLarge = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
        ),
        content = content
    )
}
