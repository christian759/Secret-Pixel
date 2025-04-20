package com.example.secretpixel.ui

import android.content.Context
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

val Context.dataStore by preferencesDataStore(name = "settings")

object ThemeManager{
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    fun darkModeFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map{ prefs ->
            prefs[DARK_MODE_KEY] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean){
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}

val LightTheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    background = Color(0xFFF6F6F6),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF4DD0E1),
    onPrimary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
)
