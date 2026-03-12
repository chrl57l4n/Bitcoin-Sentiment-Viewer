# Bitcoin Dashboard – Android App

Native Android Bitcoin Dashboard in **Kotlin + Jetpack Compose** mit Neon/Dark-UI.

## Features

- 📈 **Live BTC-Preis** (Binance API, 10-Sekunden-Update)
- 🕯️ **Preis-Chart** mit 5 Timeframes (1h · 30T · 6M · 1J · 5J)
- 🧠 **Marktsentiment** (Retail / Institutionell / BlackRock IBIT)
- 🏦 **BTC auf Börsen** (Exchange Reserve, Dual-Y-Achse)
- 😱 **Fear & Greed Index** (alternative.me)
- 📰 **News** (Blocktrainer RSS-Feed)

## Tech Stack

| Layer | Technologie |
|---|---|
| UI | Jetpack Compose + Material3 |
| Charts | Custom Canvas Drawing |
| Architektur | MVVM (ViewModel + StateFlow) |
| Netzwerk | Retrofit2 + OkHttp3 + Gson |
| Async | Kotlin Coroutines |

## Voraussetzungen

- Android Studio Ladybug (2024.2) oder neuer
- JDK 17
- Android SDK 35 (Ziel), minSdk 26 (Android 8.0)
- Snapdragon 8 Gen 3 / ARM64 optimiert

## Build & Run

```bash
git clone https://github.com/DEIN_USERNAME/bitcoin-dashboard-android
cd BitcoinDashboard
./gradlew assembleDebug
```

Oder in Android Studio: **File → Open → BitcoinDashboard → Run ▶**

## Datenquellen

| Quelle | Daten |
|---|---|
| [Binance API](https://api.binance.com) | Live-Preis, Klines |
| [CryptoCompare](https://min-api.cryptocompare.com) | Histodaten, Sentiment |
| [Alternative.me](https://api.alternative.me/fng) | Fear & Greed Index |
| [Yahoo Finance](https://query1.finance.yahoo.com) | BlackRock IBIT ETF |
| [Blocktrainer](https://www.blocktrainer.de/feed) | Bitcoin-News |

## Projektstruktur

```
app/src/main/java/com/bitcoin/dashboard/
├── data/
│   ├── api/         # Retrofit API-Interfaces
│   ├── model/       # Datenklassen
│   └── repository/  # Datenabruf & Parsing
├── ui/
│   ├── components/  # Charts, Cards, Gauges
│   ├── screens/     # DashboardScreen
│   └── theme/       # Farben, Typografie
├── viewmodel/       # DashboardViewModel
└── MainActivity.kt
```

## Lizenz

MIT
