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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyVideoScreen(onBack: () -> Unit) {
    var isAnalyzing by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedVideoUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAnalyzing) "Video Analysis" else "Verify Video", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAnalyzing) {
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(22.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isAnalyzing) {
                VerifyVideoInputState(
                    urlText = urlText,
                    onUrlChange = { urlText = it },
                    onAnalyze = { isAnalyzing = true },
                    selectedVideoUri = selectedVideoUri,
                    onUploadClick = {
                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                )
            } else {
                VerifyVideoAnalysisState()
            }
        }
    }
}

@Composable
fun ColumnScope.VerifyVideoInputState(
    urlText: String,
    onUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    selectedVideoUri: Uri?,
    onUploadClick: () -> Unit
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
            .clickable { onUploadClick() },
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
                    "Drag and drop folder or click to browse",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Upload", fontWeight = FontWeight.SemiBold)
                }
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
                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark)
                ) {
                    Text("Change Video", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
    
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

    Spacer(Modifier.height(24.dp))

    // Link Input
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Paste Video Link",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = urlText,
            onValueChange = onUrlChange,
            placeholder = { Text("https://youtube.com/watch?v=...", color = TextTertiary) },
            leadingIcon = {
                Icon(Icons.Filled.Link, contentDescription = null, tint = TextTertiary)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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

    Spacer(Modifier.weight(1f))

    // Bottom action
    Button(
        onClick = onAnalyze,
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        enabled = selectedVideoUri != null || urlText.isNotBlank()
    ) {
        Icon(Icons.Filled.VideoFile, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Analyze Video", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun VerifyVideoAnalysisState() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Video Player Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            // Simulated video thumbnail/overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // High Risk Badge in top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DangerRed)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("HIGH RISK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Play button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // Time and progress bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:37", color = Color.White, fontSize = 12.sp)
                    Text("2:23", color = Color.White, fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(4.dp)
                            .background(AccentBlue)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreCard(
                title = "Credibility Score",
                score = "42",
                target = "/100",
                color = DangerRed,
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                title = "Deepfake Probability",
                score = "85",
                target = "%",
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Analysis Timeline
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Analytics, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Analysis Timeline", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        TimelineItem(
            timeLabel = "0:12 - 0:45",
            title = "AI-Generated Faces",
            description = "High confidence detection",
            badge = "MANIPULATED",
            badgeColor = DangerRed,
            dotColor = DangerRed
        )
        TimelineItem(
            timeLabel = "1:05 - 1:18",
            title = "Manipulated Audio",
            description = "Synthetic voice detected",
            badge = "MODIFIED",
            badgeColor = WarningOrange,
            dotColor = DangerRed
        )
        TimelineItem(
            timeLabel = "1:19 - End",
            title = "Authentic Footage",
            description = "Stock background verified",
            badge = null,
            badgeColor = SuccessGreen,
            dotColor = AccentBlue
        )

        Spacer(Modifier.height(24.dp))

        // Original Source
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AccentBlue.copy(alpha = 0.1f))
                .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Original Source Found", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Matching footage discovered from 2022", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 24.dp))
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceVariantDark)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Evening News: Economic Summit Highlight Reel", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("youtube.com/watch?v=kXp9...", color = AccentBlue, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Lateral Reading
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountTree, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Lateral Reading & Cross-Reference", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        SourceItem(
            source = "BBC News",
            status = "CONTRADICTS",
            statusColor = DangerRed,
            title = "Investigation reveals inconsistencies in recent economic summit footage..."
        )
        SourceItem(
            source = "Reuters",
            status = "CORROBORATES",
            statusColor = SuccessGreen,
            title = "Verified: Global leaders discuss summit outcomes in official briefing..."
        )
        SourceItem(
            source = "AP News",
            status = "CORROBORATES",
            statusColor = SuccessGreen,
            title = "Fact Check: Original audio logs confirm key statements from meeting..."
        )
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ScoreCard(title: String, score: String, target: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(score, color = color, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(target, color = TextTertiary, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun TimelineItem(
    timeLabel: String,
    title: String,
    description: String,
    badge: String?,
    badgeColor: Color,
    dotColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            // Vertical line connection could go here in a real app
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(SurfaceVariantDark)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (badge != null) {
                    Text(
                        badge,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeLabel, color = TextTertiary, fontSize = 12.sp)
                Text(" • ", color = TextTertiary, fontSize = 12.sp)
                Text(description, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SourceItem(
    source: String,
    status: String,
    statusColor: Color,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SurfaceVariantDark),
            contentAlignment = Alignment.Center
        ) {
            Text(source.take(1), color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(source, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(title, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
}
