package com.example.readmeviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.readmeviewer.ui.components.MarkdownWebView
import com.example.readmeviewer.viewmodel.MainViewModel

@Composable
fun FullScreenScreen(
    viewModel: MainViewModel,
    onExitFullScreen: () -> Unit
) {
    val markdownText by viewModel.markdownText.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    // Dynamic background color based on theme
    val backgroundColor = if (isDarkMode) Color.Black else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Full-screen markdown content with tap to exit
        Box(modifier = Modifier.fillMaxSize()) {
            MarkdownWebView(
                markdownText = markdownText,
                isDarkMode = isDarkMode,
                fontSize = fontSize,
                modifier = Modifier.fillMaxSize()
            )
            
            // Transparent overlay for tap detection to exit full screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onExitFullScreen() }
                        )
                    }
            )
        }
        
        // Simple close button in top right corner, background
        Box(
            modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(if (isDarkMode) Color.Black else Color.White),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
            onClick = onExitFullScreen,
            modifier = Modifier.size(40.dp)
            ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Full Screen",
                tint = contentColor
            )
            }
        }
    }
}