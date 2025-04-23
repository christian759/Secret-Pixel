package com.example.secretpixel.ui


import androidx.compose.ui.graphics.Color

data class ColorTheme(val backgroundColor: Color, val textColor: Color, val cardColor: Color, val cardColor2: Color)

val colorScheme: ColorTheme = ColorTheme(backgroundColor = Color(0xFFCDCED0),
    textColor = Color(0xFF1B1B1B),
   cardColor = Color(0xFFA3ADAD),
    cardColor2 = Color(0xFFE7E7E7))