package com.example.readmeviewer.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readmeviewer.ui.components.MarkdownWebView
import com.example.readmeviewer.ui.components.SettingsDialog
import com.example.readmeviewer.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToFullScreen: () -> Unit,
    isDarkMode: Boolean,
    toggleDarkMode: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val markdownText by viewModel.markdownText.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.selectFile(it) }
    }

    // Dynamic background color based on theme
    val backgroundColor = if (isDarkMode) Color.Black else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Header with controls (matching iOS design)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = backgroundColor,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Back button (when content is loaded)
                    Box(modifier = Modifier.width(48.dp)) {
                        if (markdownText.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    viewModel.selectFile(android.net.Uri.EMPTY) // Clear content
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = contentColor
                                )
                            }
                        }
                    }
                    
                    // Center - Title
                    Text(
                        text = if (markdownText.isEmpty()) "README Viewer" else "Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor
                    )
                    
                    // Right side - Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Font size/Settings button
                        IconButton(onClick = { viewModel.showSettings() }) {
                            Icon(
                                Icons.Default.TextFormat,
                                contentDescription = "Font Size",
                                tint = contentColor
                            )
                        }
                        
                        // Dark mode toggle
                        IconButton(onClick = toggleDarkMode) {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = contentColor
                            )
                        }
                        
                        // Export PDF button (when content is loaded)
                        if (markdownText.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.exportToPdf(
                                    onSuccess = { pdfUri ->
                                        // Share the PDF file
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                                    },
                                    onError = { error ->
                                        // Fallback to sharing plain text if PDF export fails
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, markdownText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share README"))
                                    }
                                )
                            }) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = "Export PDF",
                                    tint = contentColor
                                )
                            }
                        }
                        
                        // Full screen button (when content is loaded)
                        if (markdownText.isNotEmpty()) {
                            IconButton(onClick = onNavigateToFullScreen) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = "Full Screen",
                                    tint = contentColor
                                )
                            }
                        }
                    }
                }
            }
            
            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        // Loading State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    markdownText.isEmpty() -> {
                        // Empty State (matching iOS design)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Document icon
                            Text(
                                text = "📄",
                                fontSize = 64.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Main message
                            Text(
                                text = "No file selected",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Subtitle
                            Text(
                                text = "Tap below to open a md file",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Choose file button (matching iOS style)
                            Button(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("text/*", "application/octet-stream"))
                                },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "📂 Choose .md File",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    else -> {
                        // Content loaded state
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Markdown content
                            MarkdownWebView(
                                markdownText = markdownText,
                                isDarkMode = isDarkMode,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Simple font size slider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "A",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                                
                                Slider(
                                    value = fontSize,
                                    onValueChange = { viewModel.updateFontSize(it) },
                                    valueRange = 10f..20f,
                                    steps = 9,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                                
                                Text(
                                    text = "A",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Settings Dialog
        if (uiState.showSettings) {
            SettingsDialog(
                fontSize = fontSize,
                isDarkMode = isDarkMode,
                onFontSizeChange = { viewModel.updateFontSize(it) },
                onDarkModeToggle = { viewModel.toggleDarkMode() },
                onDismiss = { viewModel.hideSettings() }
            )
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
            
            // Error Snackbar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}