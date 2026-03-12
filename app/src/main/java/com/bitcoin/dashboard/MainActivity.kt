package com.bitcoin.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import com.bitcoin.dashboard.ui.screens.DashboardScreen
import com.bitcoin.dashboard.ui.theme.BitcoinDashboardTheme
import com.bitcoin.dashboard.ui.theme.BgDeep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitcoinDashboardTheme {
                DashboardScreen()
            }
        }
    }
}
