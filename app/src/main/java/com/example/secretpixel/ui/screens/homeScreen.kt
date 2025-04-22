package com.example.secretpixel.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.secretpixel.R

@Composable
fun homeScreen(navController: NavController){
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF515353)),
    ){
        Column{
            Spacer(modifier = Modifier.height(30.dp))

            Row(Modifier.fillMaxWidth()) {
                IconButton(
                    {navController.navigate("info")}
                ) {
                    Icon(imageVector = Icons.Filled.Info, "info", Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.weight(1f))

                Image(painter = painterResource(R.drawable.secretpixel), "secret pixel",
                    modifier = Modifier.width(50.dp))

                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    {navController.navigate("settings")}
                ) {
                    Icon(imageVector = Icons.Filled.Settings, "settings", Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.height(5.dp))
                    FeatureCard(R.drawable.hidefile1) { navController.navigate("hide_file") }
                    FeatureCard(R.drawable.extractfile1) { navController.navigate("extract_file") }
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    FeatureCard(R.drawable.hidetext1) { navController.navigate("hide_text") }
                    FeatureCard(R.drawable.extracttext1) { navController.navigate("extract_text") }
                }
            }
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
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "feature icon",
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
