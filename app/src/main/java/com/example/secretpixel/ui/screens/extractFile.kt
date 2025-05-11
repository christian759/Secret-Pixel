package com.example.secretpixel.ui.screens

import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.secretpixel.ui.colorScheme
import com.example.secretpixel.R
import com.example.secretpixel.StegoEngine
@Composable
fun extractFile(navController: NavController) {

    var stegoImageUri by remember { mutableStateOf<Uri?>(null) }
    var key by remember { mutableStateOf("") }
    val context = LocalContext.current

    val pickStegoImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> stegoImageUri = uri }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.extract_file2), // replace with your actual drawable
                contentDescription = "Secret Pixel",
                modifier = Modifier.width(400.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Stego Image Picker
            FilePickerCard(
                title = R.drawable.selectimage,
                buttonLabel = "Choose a Stego Image",
                fileName = stegoImageUri?.lastPathSegment ?: "No file selected",
                icon = Icons.Default.Image,
                onClick = { pickStegoImage.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Key field
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                maxLines = 4,
                shape = RoundedCornerShape(10.dp),
                label = { Text("Decryption key (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = colorScheme.textColor,
                    unfocusedTextColor = colorScheme.textColor.copy(alpha = 0.8f),
                    cursorColor = colorScheme.textColor,
                    focusedContainerColor = colorScheme.cardColor.copy(alpha = 0.05f),
                    unfocusedContainerColor = colorScheme.cardColor.copy(alpha = 0.02f),
                    focusedIndicatorColor = colorScheme.cardColor,
                    unfocusedIndicatorColor = colorScheme.cardColor.copy(alpha = 0.3f),
                    focusedLabelColor = colorScheme.textColor,
                    unfocusedLabelColor = colorScheme.textColor.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.25f)),
                elevation = ButtonDefaults.buttonElevation(6.dp, 2.dp),
                colors = ButtonColors(
                    containerColor = colorScheme.cardColor,
                    contentColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified
                ),
                onClick = {
                    StegoEngine.extractFile(context, stegoImageUri, key)
                }
            ) {
                Box {
                    Text("Extract File", color = colorScheme.textColor, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(Modifier.weight(1f))
        }

        TextButton(
            onClick = { navController.navigate("info") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                tint = colorScheme.cardColor,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Info",
                color = colorScheme.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
