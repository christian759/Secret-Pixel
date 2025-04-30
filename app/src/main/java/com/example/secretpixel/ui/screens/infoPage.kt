package com.example.secretpixel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.secretpixel.R
import com.example.secretpixel.ui.colorScheme

@Composable
fun infoPage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(R.drawable.secretpixel), "secret pixel",
                    modifier = Modifier.width(220.dp)
                )
            }

            Text(
                text = "A modern steganography tool to hide and extract data from images.",
                fontSize = 14.sp,
                color = colorScheme.textColor.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "🔐 Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.textColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            val features = listOf(
                "Hide text in image with optional encryption",
                "Extract hidden text",
                "Hide any type of file inside image",
                "Extract hidden file",
                "Secure with custom encryption key"
            )

            features.forEach {
                Text(
                    text = "• $it",
                    fontSize = 14.sp,
                    color = colorScheme.textColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "📂 How to Use",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.textColor
            )

            Spacer(modifier = Modifier.height(15.dp))

            listOf(
                Triple("Hide Text in Image", Icons.Default.Edit, listOf(
                    "Choose a cover image.",
                    "Type the text you want to hide.",
                    "Optionally enter a key for encryption.",
                    "Tap 'Hide Text' to embed the message."
                )),
                Triple("Extract Text from Image", Icons.Default.Visibility, listOf(
                    "Select the image with hidden text (Stego Image).",
                    "Enter the decryption key if required.",
                    "Tap 'Extract Text' to reveal the message."
                )),
                Triple("Hide File in Image", Icons.Default.AttachFile, listOf(
                    "Pick a cover image.",
                    "Choose the file you want to hide (PDF, ZIP, etc).",
                    "Optionally enter a key for encryption.",
                    "Tap 'Hide File' to embed it inside the image."
                )),
                Triple("Extract File from Image", Icons.Default.Download, listOf(
                    "Select the image that contains the hidden file (Stego Image).",
                    "Enter the key if encryption was used.",
                    "Tap 'Extract File' to retrieve your hidden file."
                ))
            ).forEach { (title, icon, steps) ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.cardColor),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = colorScheme.textColor)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        steps.forEachIndexed { i, step ->
                            Text(
                                text = "${i + 1}. $step",
                                fontSize = 14.sp,
                                color = colorScheme.textColor.copy(alpha = 0.85f),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Built with ❤️ by Secret Pixel Team",
                fontSize = 13.sp,
                color = colorScheme.textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

        }

    }
}
