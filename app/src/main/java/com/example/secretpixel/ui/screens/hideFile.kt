package com.example.secretpixel.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.secretpixel.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun hideFile(){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.secretpixel),
                        contentDescription = "secret pixel",
                        modifier = Modifier
                            .height(32.dp)
                            .padding(vertical = 4.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "back button"
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OpenFile(
                imageRes = R.drawable.imgselect,
                text = "Select a cover image",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            OpenFile(
                imageRes = R.drawable.fileselect,
                text = "Select a file to hide",
                onClick = {}
            )
        }
    }
}

@Composable
fun OpenFile(
    imageRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}