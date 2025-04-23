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

data class ColorTheme(val backgroundColor: Color, val textColor: Color, val cardColor: Color, val cardColor2: Color)

val colorScheme: ColorTheme = ColorTheme(backgroundColor = Color(0xFFCDCED0),
    textColor = Color(0xFF1B1B1B),
   cardColor = Color(0xFFA3ADAD),
    cardColor2 = Color(0xFFE7E7E7))