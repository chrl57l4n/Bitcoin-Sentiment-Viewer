package de.chri57i4n.bitcoindashboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.chri57i4n.bitcoindashboard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vollbild – kein ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(Color.parseColor("#060a10"));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webView = binding.webView;
        setupWebView();

        // SwipeRefresh – von oben wischen lädt Dashboard neu
        binding.swipeRefresh.setColorSchemeColors(Color.parseColor("#f7931a"));
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#0c1320"));
        binding.swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
            binding.swipeRefresh.setRefreshing(false);
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            loadDashboard();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // JavaScript & DOM-Speicher
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // Snapdragon 8: Hardware-Rendering erzwingen
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Caching für Offline-Fallback
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Viewport & Zoom
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);           // App-interner Touch-Zoom deaktiviert
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Sicherheit
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setGeolocationEnabled(false);

        // Performance: WebView-Renderer-Priorität
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setRendererPriorityPolicy(
                WebSettings.RENDERER_PRIORITY_IMPORTANT, true
            );
        }

        // Schrift
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMinimumFontSize(12);

        // User-Agent: Snapdragon-8-optimiert
        String ua = "BitcoinDashboard/2.0 (Android " + Build.VERSION.RELEASE
                + "; arm64; " + Build.MODEL + ") WebView";
        settings.setUserAgentString(ua);

        webView.setBackgroundColor(Color.parseColor("#060a10"));

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Alle Navigation in der App behalten
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.errorView.setVisibility(View.GONE);
                // Inject: Viewport-Höhe für Mobile-CSS
                webView.evaluateJavascript(
                    "document.documentElement.style.setProperty('--vh', window.innerHeight * 0.01 + 'px');",
                    null
                );
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                showError("Netzwerkfehler: " + description);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                // Nur Fehler loggen
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    android.util.Log.e("WebView", message.message() + " [" + message.sourceId() + ":" + message.lineNumber() + "]");
                }
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    binding.loadingView.setVisibility(View.VISIBLE);
                } else {
                    binding.loadingView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadDashboard() {
        if (isNetworkAvailable()) {
            // Lädt die HTML-Datei aus dem assets-Ordner
            webView.loadUrl("file:///android_asset/bitcoin-enhanced.html");
        } else {
            showError("Keine Internetverbindung.\nBitte Verbindung prüfen und neu laden.");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    public void onRetryClick(View v) {
        binding.errorView.setVisibility(View.GONE);
        loadDashboard();
    }

    private void showError(String message) {
        binding.errorView.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
        binding.loadingView.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
