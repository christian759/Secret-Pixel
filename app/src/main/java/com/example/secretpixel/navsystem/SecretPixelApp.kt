package com.example.secretpixel.navsystem

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.secretpixel.ui.screens.*

@Composable
fun SecretPixelApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("home"){ homeScreen(navController) }
        composable("hide_file") { hideFile(navController) }
        composable("extract_file") { extractFile(navController) }
        composable("hide_text") { hideText(navController) }
        composable("extract_text") { extractText(navController) }
        composable("info") { infoPage(navController) }
    }
}