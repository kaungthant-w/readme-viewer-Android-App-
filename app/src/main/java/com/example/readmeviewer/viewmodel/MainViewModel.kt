package com.example.readmeviewer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readmeviewer.data.MainUiState
import com.example.readmeviewer.data.PreferencesManager
import com.example.readmeviewer.data.RecentFile
import com.example.readmeviewer.repository.FileRepository
import com.example.readmeviewer.utils.MarkdownProcessor
import com.example.readmeviewer.utils.PdfExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {
    private val fileRepository = FileRepository(context)
    private val preferencesManager = PreferencesManager(context)
    private val pdfExporter = PdfExporter(context)
    private val markdownProcessor = MarkdownProcessor()
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _markdownText = MutableStateFlow("")
    val markdownText: StateFlow<String> = _markdownText.asStateFlow()
    
    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: StateFlow<List<RecentFile>> = _recentFiles.asStateFlow()
    
    // Settings state
    private val _fontSize = MutableStateFlow(14f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()
    
    init {
        loadPreferences()
        loadRecentFiles()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.getFontSize().collect { size ->
                _fontSize.value = size
            }
        }
        
        viewModelScope.launch {
            preferencesManager.getDarkMode().collect { isDark ->
                _isDarkMode.value = isDark
            }
        }
    }
    
    private fun loadRecentFiles() {
        viewModelScope.launch {
            fileRepository.getRecentFiles().collect { files ->
                _recentFiles.value = files
            }
        }
    }
    
    fun selectFile(uri: Uri) {
        viewModelScope.launch {
            // Handle clearing content (when Uri.EMPTY is passed)
            if (uri == Uri.EMPTY) {
                _markdownText.value = ""
                _uiState.value = _uiState.value.copy(selectedFileUri = null)
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            fileRepository.readMarkdownFile(uri).fold(
                onSuccess = { content ->
                    _markdownText.value = content
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedFileUri = uri
                    )
                    
                    // Add to recent files
                    val fileName = fileRepository.getFileName(uri)
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
    
    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            _fontSize.value = size
            preferencesManager.saveFontSize(size)
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newMode = !_isDarkMode.value
            _isDarkMode.value = newMode
            preferencesManager.saveDarkMode(newMode)
        }
    }
    
    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }
    
    fun showSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }
    
    fun hideSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
    }
    
    fun removeRecentFile(file: RecentFile) {
        viewModelScope.launch {
            val currentFiles = _recentFiles.value.toMutableList()
            currentFiles.remove(file)
            _recentFiles.value = currentFiles
            fileRepository.saveRecentFiles(currentFiles)
        }
    }
    
    fun exportToPdf(onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (_markdownText.value.isEmpty()) {
                onError("No content to export")
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Export to PDF - focus only on PDF format
                pdfExporter.exportMarkdownToPdfSimple(
                    _markdownText.value,
                    "README"
                ).fold(
                    onSuccess = { pdfUri ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onSuccess(pdfUri)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "PDF export failed: ${error.message}"
                        )
                        onError("PDF export failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "PDF export failed: ${e.message}"
                )
                onError("PDF export failed: ${e.message}")
            }
        }
    }
    
    fun exportTextToPdf(uri: Uri, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Read text file content
                fileRepository.readTextFile(uri).fold(
                    onSuccess = { textContent ->
                        // Export text content to PDF
                        pdfExporter.exportTextToPdf(
                            textContent,
                            "TextFile"
                        ).fold(
                            onSuccess = { pdfUri ->
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onSuccess(pdfUri)
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "PDF export failed: ${error.message}"
                                )
                                onError("PDF export failed: ${error.message}")
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to read text file: ${error.message}"
                        )
                        onError("Failed to read text file: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Text to PDF export failed: ${e.message}"
                )
                onError("Text to PDF export failed: ${e.message}")
            }
        }
    }
    
    fun exportMdToPdf(uri: Uri, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Read markdown file content
                fileRepository.readMarkdownFile(uri).fold(
                    onSuccess = { markdownContent ->
                        // Export markdown content to PDF
                        pdfExporter.exportMarkdownToPdfSimple(
                            markdownContent,
                            "MarkdownFile"
                        ).fold(
                            onSuccess = { pdfUri ->
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onSuccess(pdfUri)
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "PDF export failed: ${error.message}"
                                )
                                onError("PDF export failed: ${error.message}")
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to read markdown file: ${error.message}"
                        )
                        onError("Failed to read markdown file: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "MD to PDF export failed: ${e.message}"
                )
                onError("MD to PDF export failed: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Save current state when ViewModel is cleared
        viewModelScope.launch {
            preferencesManager.saveFontSize(_fontSize.value)
            preferencesManager.saveDarkMode(_isDarkMode.value)
            fileRepository.saveRecentFiles(_recentFiles.value)
        }
    }
}