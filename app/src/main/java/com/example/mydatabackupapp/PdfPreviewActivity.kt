package com.example.mydatabackupapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity

class PdfPreviewActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pdfUrl = intent.getStringExtra("pdfUrl")

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        pdfUrl?.let {
            val viewerUrl = "https://docs.google.com/gview?embedded=true&url=$pdfUrl"
            webView.loadUrl(viewerUrl)
        }

        setContentView(webView)
    }
}
