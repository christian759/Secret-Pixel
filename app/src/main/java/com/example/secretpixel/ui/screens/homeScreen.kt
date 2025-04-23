package com.example.secretpixel.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.secretpixel.R
import com.example.secretpixel.ui.colorScheme

@Composable
fun homeScreen(navController: NavController){

    Box(
        modifier = Modifier.fillMaxSize()
            .background(colorScheme.backgroundColor),
    ){

        Column{

            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.secretpixel), "secret pixel",
                    modifier = Modifier.width(220.dp)
                        .padding(top = 50.dp, bottom = 30.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    FeatureCard(R.drawable.hidefile1) { navController.navigate("hide_file") }
                    FeatureCard(R.drawable.extractfile1) { navController.navigate("extract_file") }
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    Spacer(Modifier.height(15.dp))
                    FeatureCard(R.drawable.hidetext1) { navController.navigate("hide_text") }
                    FeatureCard(R.drawable.extracttext1) { navController.navigate("extract_text") }
                }
            }
        }
        TextButton(onClick = {navController.navigate("info")},
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(20.dp)) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = colorScheme.cardColor,
                modifier = Modifier.padding(10.dp))
            Text(
                text = "Info",
                color = colorScheme.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeatureCard(
    imageRes: Int,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.cardColor),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .clickable {
                    onClick()
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "feature icon",
                modifier = Modifier.size(85.dp)
            )
        }
    }
}
