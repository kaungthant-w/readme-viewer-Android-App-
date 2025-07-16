package com.example.readmeviewer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("font_size")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val RECENT_FILES_KEY = stringPreferencesKey("recent_files")
    }
    
    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
    
    fun getFontSize(): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[FONT_SIZE_KEY] ?: 14f
        }
    }
    
    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
    
    fun getDarkMode(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
    }
    
    suspend fun saveRecentFiles(files: List<RecentFile>) {
        val json = Gson().toJson(files)
        context.dataStore.edit { preferences ->
            preferences[RECENT_FILES_KEY] = json
        }
    }
    
    fun getRecentFiles(): Flow<List<RecentFile>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[RECENT_FILES_KEY] ?: ""
            if (json.isNotEmpty()) {
                try {
                    Gson().fromJson(json, object : TypeToken<List<RecentFile>>() {}.type)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
}