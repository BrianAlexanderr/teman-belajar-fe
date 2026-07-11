package com.example.teman_belajar.folderdetail

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class WebViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)

        setContentView(webView)

        webView.settings.javaScriptEnabled = true

        webView.settings.domStorageEnabled = true

        webView.settings.builtInZoomControls = true

        webView.settings.displayZoomControls = false

        webView.webViewClient = WebViewClient()

        val url = intent.getStringExtra("url")

        if (url != null) {
            webView.loadUrl(url)
        }
    }
}
