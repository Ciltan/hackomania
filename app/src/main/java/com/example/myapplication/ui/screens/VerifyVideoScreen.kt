package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyVideoScreen(
    onBack: () -> Unit,
    onAnalyze: (String?, Uri?) -> Unit,
    isLoading: Boolean = false
) {
    var urlText by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedVideoUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Video", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { onAnalyze(urlText.takeIf { it.isNotBlank() }, selectedVideoUri) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = (selectedVideoUri != null || urlText.isNotBlank()) && !isLoading
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Analyzing video... This may take a while", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VideoFile, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Analyze Video", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Fact-checking engine is active. Upload or link a video to detect deepfakes or misinformation.",
                color = AccentBlue,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Drag and drop area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .background(SurfaceVariantDark)
                    .clickable { 
                        if (!isLoading) videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    if (selectedVideoUri == null) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.VideoFile,
                                contentDescription = "Upload Video",
                                tint = AccentBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Select Video File",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Click to browse or upload your video",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = SuccessGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Video Selected",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ready for analysis",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Change Video",
                            color = AccentBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { 
                                if (!isLoading) videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            
            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariantDark)
                Text(
                    "OR USE A LINK",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariantDark)
            }

            Spacer(Modifier.height(32.dp))

            // Link Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Video Link",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    placeholder = { Text("https://youtube.com/watch?v=...", color = TextTertiary) },
                    leadingIcon = {
                        Icon(Icons.Filled.Link, contentDescription = null, tint = TextTertiary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedContainerColor = SurfaceVariantDark,
                        unfocusedContainerColor = SurfaceVariantDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}
