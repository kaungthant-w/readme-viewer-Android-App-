package com.example.readmeviewer.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.readmeviewer.utils.MarkdownProcessor

@Composable
fun MarkdownWebView(
    markdownText: String,
    isDarkMode: Boolean,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    val markdownProcessor = remember { MarkdownProcessor() }
    
    val htmlContent = remember(markdownText, isDarkMode, fontSize) {
        markdownProcessor.processMarkdown(markdownText, isDarkMode, fontSize)
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = false
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                    displayZoomControls = false
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}