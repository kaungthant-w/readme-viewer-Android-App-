# Android Logic Instructions for README Viewer App

## Overview
This document outlines the logic implementation for converting the iOS README Viewer app to Android, focusing on state management, data flow, and business logic.

## Architecture Pattern
Use **MVVM (Model-View-ViewModel)** with Jetpack Compose and the following components:
- **ViewModel**: Business logic and state management
- **Repository**: Data access layer
- **Use Cases**: Business operations
- **Compose UI**: Reactive UI layer

## Core Dependencies
```kotlin
// build.gradle (app level)
dependencies {
    // Compose BOM
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    // Activity Compose
    implementation 'androidx.activity:activity-compose:1.8.2'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.6'
    
    // WebView
    implementation 'androidx.webkit:webkit:1.9.0'
    
    // File operations
    implementation 'androidx.documentfile:documentfile:1.0.1'
    
    // Preferences
    implementation 'androidx.datastore:datastore-preferences:1.0.0'
    
    // Markdown processing
    implementation 'io.noties.markwon:core:4.6.2'
    implementation 'io.noties.markwon:html:4.6.2'
    implementation 'io.noties.markwon:syntax-highlight:4.6.2'
    
    // PDF generation
    implementation 'com.itextpdf:itext7-core:7.2.5'
}
```

## State Management

### 1. Main ViewModel
```kotlin
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _markdownText = MutableStateFlow("")
    val markdownText: StateFlow<String> = _markdownText.asStateFlow()
    
    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()
    
    // Settings state
    private val _fontSize = MutableStateFlow(16f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()
}

data class MainUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val showSettings: Boolean = false,
    val showShareSheet: Boolean = false,
    val selectedFileUri: Uri? = null,
    val error: String? = null
)

data class RecentFile(
    val uri: Uri,
    val name: String,
    val lastAccessed: Long
)
```

### 2. File Operations Logic
```kotlin
class FileRepository(private val context: Context) {
    
    suspend fun readMarkdownFile(uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }
            Result.success(content ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveRecentFile(file: RecentFile) {
        // Save to DataStore or Room database
        preferencesManager.saveRecentFile(file)
    }
    
    suspend fun getRecentFiles(): List<RecentFile> {
        return preferencesManager.getRecentFiles()
    }
    
    suspend fun exportToPdf(htmlContent: String): Result<ByteArray> {
        return try {
            val pdfBytes = PdfGenerator.generateFromHtml(htmlContent)
            Result.success(pdfBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Preferences Management
```kotlin
class PreferencesManager(private val context: Context) {
    private val dataStore = context.dataStore
    
    companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("font_size")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val RECENT_FILES_KEY = stringPreferencesKey("recent_files")
    }
    
    suspend fun saveFontSize(size: Float) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
    
    fun getFontSize(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[FONT_SIZE_KEY] ?: 16f
        }
    }
    
    suspend fun saveDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
    
    fun getDarkMode(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    }
    
    suspend fun saveRecentFiles(files: List<RecentFile>) {
        val json = Gson().toJson(files)
        dataStore.edit { preferences ->
            preferences[RECENT_FILES_KEY] = json
        }
    }
    
    fun getRecentFiles(): Flow<List<RecentFile>> {
        return dataStore.data.map { preferences ->
            val json = preferences[RECENT_FILES_KEY] ?: ""
            if (json.isNotEmpty()) {
                Gson().fromJson(json, object : TypeToken<List<RecentFile>>() {}.type)
            } else {
                emptyList()
            }
        }
    }
}
```

## Business Logic Implementation

### 1. File Selection Logic
```kotlin
// In ViewModel
fun selectFile(uri: Uri) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        fileRepository.readMarkdownFile(uri).fold(
            onSuccess = { content ->
                _markdownText.value = content
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedFileUri = uri
                )
                
                // Add to recent files
                val fileName = getFileName(uri)
                val recentFile = RecentFile(uri, fileName, System.currentTimeMillis())
                addToRecentFiles(recentFile)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        )
    }
}

private suspend fun addToRecentFiles(file: RecentFile) {
    val currentFiles = _recentFiles.value.toMutableList()
    
    // Remove if already exists
    currentFiles.removeAll { it.uri == file.uri }
    
    // Add to beginning
    currentFiles.add(0, file)
    
    // Keep only last 5 files
    if (currentFiles.size > 5) {
        currentFiles.removeAt(currentFiles.size - 1)
    }
    
    _recentFiles.value = currentFiles
    fileRepository.saveRecentFiles(currentFiles)
}
```

### 2. Markdown Processing Logic
```kotlin
class MarkdownProcessor {
    
    fun processMarkdown(
        markdownText: String,
        isDarkMode: Boolean,
        fontSize: Float
    ): String {
        val markwon = Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(SyntaxHighlightPlugin.create(Prism4j(), Prism4jTheme.create()))
            .build()
        
        val document = markwon.parse(markdownText)
        val html = markwon.render(document)
        
        return generateStyledHtml(html.toString(), isDarkMode, fontSize)
    }
    
    private fun generateStyledHtml(
        content: String,
        isDarkMode: Boolean,
        fontSize: Float
    ): String {
        val bgColor = if (isDarkMode) "#1e1e1e" else "#ffffff"
        val textColor = if (isDarkMode) "#d4d4d4" else "#000000"
        val codeBg = if (isDarkMode) "#2d2d2d" else "#f4f4f4"
        val linkColor = if (isDarkMode) "#9cdcfe" else "#0066cc"
        val borderColor = if (isDarkMode) "#444" else "#ddd"
        
        val css = """
            <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background-color: $bgColor;
                color: $textColor;
                padding: 20px;
                line-height: 1.6;
                font-size: ${fontSize}px;
                margin: 0;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 1.5em;
                margin-bottom: 0.5em;
            }
            h1 { 
                font-size: 2em; 
                border-bottom: 1px solid $borderColor; 
                padding-bottom: 0.3em; 
            }
            pre {
                background: $codeBg;
                padding: 16px;
                border-radius: 6px;
                overflow-x: auto;
                font-family: 'Courier New', monospace;
                font-size: ${fontSize * 0.9}px;
            }
            code {
                background: $codeBg;
                padding: 2px 4px;
                border-radius: 3px;
                font-family: 'Courier New', monospace;
                font-size: ${fontSize * 0.9}px;
            }
            a { color: $linkColor; }
            blockquote {
                border-left: 4px solid $borderColor;
                padding-left: 16px;
                margin-left: 0;
                color: ${if (isDarkMode) "#aaa" else "#666"};
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 1em 0;
            }
            th, td {
                border: 1px solid $borderColor;
                padding: 8px;
                text-align: left;
            }
            th {
                background-color: ${if (isDarkMode) "#2a2a2a" else "#f7f7f7"};
            }
            img {
                max-width: 100%;
                height: auto;
            }
            </style>
        """
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                $css
            </head>
            <body>
                $content
            </body>
            </html>
        """
    }
}
```

### 3. PDF Export Logic
```kotlin
class PdfExporter {
    
    suspend fun exportWebViewToPdf(
        webView: WebView,
        fileName: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val printAdapter = webView.createPrintDocumentAdapter(fileName)
            val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$fileName.pdf")
            
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
            
            // Create PDF using print framework
            val callback = object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                    // Handle layout finished
                }
            }
            
            printAdapter.onLayout(null, printAttributes, null, callback, null)
            
            Result.success(Uri.fromFile(pdfFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4. Navigation Logic
```kotlin
// Navigation state management
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recent : Screen("recent")
    object FullScreen : Screen("fullscreen")
}

// In MainActivity
@Composable
fun MainNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToFullScreen = {
                    navController.navigate(Screen.FullScreen.route)
                }
            )
        }
        
        composable(Screen.Recent.route) {
            RecentScreen(
                viewModel = viewModel,
                onFileSelected = { uri ->
                    viewModel.selectFile(uri)
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        
        composable(Screen.FullScreen.route) {
            FullScreenScreen(
                viewModel = viewModel,
                onExitFullScreen = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

### 5. Error Handling Logic
```kotlin
// In ViewModel
private fun handleError(error: Throwable) {
    val errorMessage = when (error) {
        is FileNotFoundException -> "File not found"
        is SecurityException -> "Permission denied"
        is IOException -> "Failed to read file"
        else -> error.message ?: "Unknown error occurred"
    }
    
    _uiState.value = _uiState.value.copy(
        error = errorMessage,
        isLoading = false
    )
}

// Clear error after showing
fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
}
```

### 6. Lifecycle Management
```kotlin
// In ViewModel
override fun onCleared() {
    super.onCleared()
    // Clean up resources
    viewModelScope.cancel()
}

// Save state when app goes to background
fun saveCurrentState() {
    viewModelScope.launch {
        preferencesManager.saveFontSize(_fontSize.value)
        preferencesManager.saveDarkMode(_isDarkMode.value)
        fileRepository.saveRecentFiles(_recentFiles.value)
    }
}
```

## Key Logic Differences from iOS

1. **State Management**: Use StateFlow instead of @State
2. **File Access**: Android's Storage Access Framework instead of iOS document picker
3. **Persistence**: DataStore instead of UserDefaults
4. **PDF Export**: Android Print Framework instead of WKWebView PDF creation
5. **Navigation**: Jetpack Navigation instead of TabView
6. **Lifecycle**: Android Activity/Fragment lifecycle management
7. **Permissions**: Handle storage permissions explicitly
8. **Background Processing**: Use Coroutines with proper scope management

## Implementation Order
1. Set up MVVM architecture with ViewModel
2. Implement file selection and reading logic
3. Add markdown processing with WebView
4. Implement settings persistence with DataStore
5. Add recent files management
6. Implement PDF export functionality
7. Add proper error handling and loading states
8. Implement navigation between screens
9. Add lifecycle management
10. Optimize performance and memory usage