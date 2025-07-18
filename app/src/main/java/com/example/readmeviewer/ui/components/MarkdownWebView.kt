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
    modifier: Modifier = Modifier,
    onSingleTap: (() -> Unit)? = null
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
                    javaScriptEnabled = true // Enable JavaScript for tap detection
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                
                // Add JavaScript interface for single tap detection
                onSingleTap?.let { tapCallback ->
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun onSingleTap() {
                            // Post to main thread
                            post { tapCallback() }
                        }
                    }, "AndroidInterface")
                }
            }
        },
        update = { webView ->
            val finalHtmlContent = if (onSingleTap != null) {
                // Add JavaScript for precise single tap detection
                htmlContent.replace(
                    "</body>",
                    """
                    <script>
                        let tapTimeout;
                        let tapCount = 0;
                        let lastTapTime = 0;
                        let startY = 0;
                        let startX = 0;
                        let hasMoved = false;
                        
                        document.addEventListener('touchstart', function(e) {
                            startY = e.touches[0].clientY;
                            startX = e.touches[0].clientX;
                            hasMoved = false;
                        }, { passive: true });
                        
                        document.addEventListener('touchmove', function(e) {
                            const moveY = Math.abs(e.touches[0].clientY - startY);
                            const moveX = Math.abs(e.touches[0].clientX - startX);
                            
                            // If moved more than 10px, it's a scroll/drag
                            if (moveY > 10 || moveX > 10) {
                                hasMoved = true;
                            }
                        }, { passive: true });
                        
                        document.addEventListener('touchend', function(e) {
                            // Ignore if it was a scroll/drag
                            if (hasMoved) {
                                return;
                            }
                            
                            // Ignore if clicking on links
                            if (e.target.tagName.toLowerCase() === 'a') {
                                return;
                            }
                            
                            const currentTime = new Date().getTime();
                            const timeDiff = currentTime - lastTapTime;
                            
                            if (timeDiff < 300 && timeDiff > 0) {
                                // This is a double tap, ignore it
                                tapCount++;
                                clearTimeout(tapTimeout);
                                return;
                            }
                            
                            tapCount = 1;
                            lastTapTime = currentTime;
                            
                            // Wait to see if there's a second tap
                            tapTimeout = setTimeout(function() {
                                if (tapCount === 1) {
                                    // Single tap confirmed
                                    AndroidInterface.onSingleTap();
                                }
                                tapCount = 0;
                            }, 300);
                        }, { passive: true });
                    </script>
                    </body>
                    """.trimIndent()
                )
            } else {
                htmlContent
            }
            
            webView.loadDataWithBaseURL(
                null,
                finalHtmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}