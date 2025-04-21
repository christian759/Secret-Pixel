package com.example.secretpixel.navsystem

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.secretpixel.ui.ThemeManager
import com.example.secretpixel.ui.screens.*

@Composable
fun SecretPixelApp() {
    val context = LocalContext.current
    val isDarkMode by ThemeManager.darkModeFlow(context).collectAsState(false)

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("home"){ homeScreen() }
        composable("hide_file") { hideFile() }
        composable("extract_file") { extractFile() }
        composable("hide_text") { hideText() }
        composable("extract_text") { extractText() }
        composable("settings") { settingsPage() }
    }
}