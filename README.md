# ₿ Bitcoin Dashboard – Android App

Eine native Android-WebView-App für das Bitcoin Sentiment Dashboard.  
Optimiert für **Snapdragon 8** (arm64-v8a).

## Projektstruktur

```
bitcoin-dashboard-android/
├── .github/workflows/build.yml   ← CI/CD: APK automatisch bauen
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── bitcoin-enhanced.html   ← ← ← HIER die HTML-Datei ablegen!
│   │   ├── java/.../MainActivity.java
│   │   └── res/
│   └── build.gradle
├── gradle.properties
└── settings.gradle
```

## Setup

### 1. Repository klonen
```bash
git clone https://github.com/chri57i4n/bitcoin-sentiment-dashboard-2.0
cd bitcoin-sentiment-dashboard-2.0
```

### 2. HTML-Dashboard einfügen
```bash
cp bitcoin-enhanced.html app/src/main/assets/
```

### 3. App bauen
```bash
./gradlew assembleDebug
```
APK liegt in: `app/build/outputs/apk/debug/`

### 4. Auf Gerät installieren
```bash
adb install app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

## GitHub Actions

Jeder Push auf `main` baut automatisch Debug- und Release-APK.  
Ein neuer Tag `v1.2.3` erstellt zusätzlich ein GitHub Release mit APK-Download.

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Snapdragon 8 Optimierungen

- **ABI:** Nur `arm64-v8a` – kein x86-Overhead
- **minSdk 26** – alle Snapdragon-8-Geräte (Android 8+)
- **Hardware-Rendering:** `LAYER_TYPE_HARDWARE` erzwungen
- **Renderer-Priorität:** `RENDERER_PRIORITY_IMPORTANT`
- **ProGuard:** Shrinking + Minification im Release

## Features

- 🔄 **SwipeRefresh** – von oben wischen zum Neu laden
- 📴 **Offline-Erkennung** – Fehlermeldung bei fehlendem Netz
- 🌑 **Vollbild-Modus** – kein ActionBar, dunkle Statusleiste
- 🔒 **HTTPS-only** – Network Security Config
- 💾 **State-Restore** – Orientierungswechsel ohne Reload
