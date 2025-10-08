package com.ceo3.secretpixel.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.ceo3.secretpixel.ui.colorScheme
import com.example.secretpixel.R
import com.ceo3.secretpixel.StegoEngine


@Composable
fun hideFile(navController: NavController) {

    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var fileToHideUri by remember { mutableStateOf<Uri?>(null) }
    var key by remember { mutableStateOf("") }
    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> coverImageUri = uri }
    }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> fileToHideUri = uri }
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(colorScheme.backgroundColor),) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.hidefile2__2_), "secret pixel",
                modifier = Modifier.width(200.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Image Picker
            FilePickerCard(
                title = R.drawable.selectimage,
                buttonLabel = "Choose an Image",
                fileName = coverImageUri?.lastPathSegment ?: "No file selected",
                icon = Icons.Default.Image,
                onClick = { pickImage.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // File Picker
            FilePickerCard(
                title = R.drawable.selectfile,
                buttonLabel = "Pick a File",
                fileName = fileToHideUri?.lastPathSegment ?: "No file selected",
                icon = Icons.Default.AttachFile,
                onClick = { pickFile.launch("*/*") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Output name field
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                maxLines = 4,
                shape = RoundedCornerShape(10.dp),
                label = { Text("Encryption key (Optional)") },
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
                colors = ButtonColors(containerColor = colorScheme.cardColor, contentColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified, disabledContainerColor = Color.Unspecified),
               onClick =  {
                    StegoEngine.hideFile(context, coverImageUri, fileToHideUri, key)
               }
            ) {
                Box{
                    Text("Hide File", color = colorScheme.textColor, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(Modifier.weight(1f))

        }


        TextButton(
            onClick = { navController.navigate("info") },
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(20.dp)
        )
        {
            Icon(
                Icons.Default.Info, contentDescription = "Info", tint = colorScheme.cardColor,
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

@Composable
fun FilePickerCard(
    title: Int,
    buttonLabel: String,
    fileName: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.cardColor),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Image(painter = painterResource(title), contentDescription = "select image",
                modifier = Modifier.width(200.dp))
            Spacer(Modifier.height(5.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Icon(icon, contentDescription = null, tint = colorScheme.textColor)
                Spacer(Modifier.width(6.dp))
                Text(buttonLabel, color = colorScheme.textColor)
            }
            Spacer(Modifier.height(8.dp))
            if (fileName == "No file selected" || fileName == "No image selected") {
                Text("Selected: $fileName", fontSize = 12.sp, color = Color.Red.copy(0.5f))
            }else{
                Text("Selected: $fileName", fontSize = 12.sp, color = colorScheme.textColor.copy(alpha = 0.7f))
            }
        }
    }
}
