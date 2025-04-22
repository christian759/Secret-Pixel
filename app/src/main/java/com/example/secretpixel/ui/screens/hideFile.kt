package com.example.secretpixel.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.secretpixel.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun hideFile(){
    TopAppBar(
        {
            Image(painter = painterResource(R.drawable.secretpixel), contentDescription = "secret pixel")
        },
        navigationIcon = {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowRight, "back button")
        }
    )
    Column {

    }
}

@Composable
fun openFile(){

}