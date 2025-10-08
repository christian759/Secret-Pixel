package com.ceo3.secretpixel.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.ceo3.secretpixel.ui.colorScheme
import com.example.secretpixel.R
import com.ceo3.secretpixel.StegoEngine

@Composable
fun extractText(navController: NavController) {

    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var key by remember { mutableStateOf("") }
    var extractedText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> coverImageUri = uri }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.backgroundColor)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.extract_text2), "secret pixel",
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            FilePickerCard(
                title = R.drawable.selectimage,
                buttonLabel = "Choose a Stego Image",
                fileName = coverImageUri?.lastPathSegment ?: "No image selected",
                icon = Icons.Default.Image,
                onClick = { pickImage.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Decryption Key (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
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
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.cardColor),
                onClick = {
                    extractedText = StegoEngine.extractText(context, coverImageUri, key)
                }
            ) {
                Box {
                    Text("Extract Text", color = colorScheme.textColor, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            extractedText?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer {
                        Text(
                            text = "Extracted text: $it",
                            color = colorScheme.textColor,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = colorScheme.textColor)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }

        TextButton(
            onClick = { navController.navigate("info") },
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
        ) {
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
