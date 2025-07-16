package com.example.readmeviewer.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.readmeviewer.data.PreferencesManager
import com.example.readmeviewer.data.RecentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepository(private val context: Context) {
    private val preferencesManager = PreferencesManager(context)
    
    suspend fun readMarkdownFile(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }
            Result.success(content ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveRecentFiles(files: List<RecentFile>) {
        preferencesManager.saveRecentFiles(files)
    }
    
    fun getRecentFiles() = preferencesManager.getRecentFiles()
    
    fun getFileName(uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        it.getString(displayNameIndex)
                    } else {
                        uri.lastPathSegment ?: "Unknown File"
                    }
                } else {
                    uri.lastPathSegment ?: "Unknown File"
                }
            } ?: (uri.lastPathSegment ?: "Unknown File")
        } catch (e: Exception) {
            uri.lastPathSegment ?: "Unknown File"
        }
    }
}