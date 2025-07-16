package com.example.readmeviewer.data

import android.net.Uri

data class MainUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val showSettings: Boolean = false,
    val showShareSheet: Boolean = false,
    val selectedFileUri: Uri? = null,
    val error: String? = null
)