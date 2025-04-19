package com.example.secretpixel.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.secretpixel.StegoEngine
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun SecretPixelApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "hide",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("hide") { HidePage() }
            composable("extract") { ExtractPage() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("hide") },
            label = { Text("Hide") },
            icon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("extract") },
            label = { Text("Extract") },
            icon = { Icon(Icons.Default.Visibility, contentDescription = null) }
        )
    }
}

@Composable
fun HidePage() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        fileUri = it
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Select Image")
        }

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )
        }

        Button(onClick = { filePicker.launch("*/*") }) {
            Text("Select File")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (imageUri != null && fileUri != null) {
                val output = StegoEngine.hideFileInImage(context, imageUri!!, fileUri!!)
                message = "Hidden file in image: ${output.name}"
            } else {
                message = "Select image and file first."
            }
        }) {
            Text("Hide File")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(message)
    }
}

@Composable
fun ExtractPage() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Select Stego Image")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (imageUri != null) {
                val file = StegoEngine.extractFileFromImage(context, imageUri!!)
                message = "Extracted file: ${file.name}"
            } else {
                message = "Select an image first."
            }
        }) {
            Text("Extract File")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(message)
    }
}
