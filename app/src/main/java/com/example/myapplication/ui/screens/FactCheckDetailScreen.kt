package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactCheckDetailScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fact-Check Detail", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextSecondary
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
        ) {
            // Header Image Placeholder (Replace with actual image loading in real app)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(SurfaceVariantDark)
            ) {
                // Here you would use AsyncImage from Coil to load the actual image.
                // For now, doing a styled placeholder.
                Box(
                    modifier = Modifier.fillMaxSize().background(AccentBlue.copy(alpha=0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                // Topic & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TRENDING RUMOR",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Reported 2h ago",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                
                // Title
                Text(
                    text = "Rumor: The Central Dam has breached and a 10ft wave is approaching downtown",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(16.dp))
                
                // Source Quote
                Text(
                    text = "\"Viral audio clip circulating on messaging apps claiming structural failure at the North Reservoir.\"",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                // Verification Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DangerContainer)
                        .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VERIFICATION STATUS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("FALSE", color = DangerRed, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.width(8.dp))
                            Text("Confidence: 100%", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Our team and local authorities have confirmed the infrastructure is intact.",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Quick Summary
                SectionHeaderRow(icon = Icons.Filled.Article, title = "Quick Summary", iconTint = AccentBlue)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "The rumors regarding the Central Dam breach are completely unfounded. Heavy rainfall has caused minor surface runoff, which may have been misinterpreted. Sensors at the North Reservoir show normal pressure levels and structural integrity.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(28.dp))

                // Official Evidence
                SectionHeaderRow(icon = Icons.Filled.Verified, title = "Official Evidence", iconTint = SuccessGreen)
                Spacer(Modifier.height(12.dp))
                
                EvidenceItem(
                    source = "DEPT. OF WATER MANAGEMENT",
                    title = "Official Status Report #402",
                    quote = "\"Structural integrity confirmed at 4:30 PM. No spillway activation required.\""
                )
                Spacer(Modifier.height(12.dp))
                EvidenceItem(
                    source = "CITY POLICE DEPARTMENT",
                    title = "Public Safety Announcement",
                    quote = "\"Officers on-site report dry conditions in the reservoir perimeter.\""
                )

                Spacer(Modifier.height(28.dp))

                // Safe Actions
                SectionHeaderRow(icon = Icons.Filled.Shield, title = "Safe Actions", iconTint = AccentBlue)
                Spacer(Modifier.height(16.dp))
                
                ActionItem(text = "Do not evacuate the downtown area unless officially instructed by sirens.")
                Spacer(Modifier.height(12.dp))
                ActionItem(text = "Stop the spread: Delete the unverified audio clip and share this official status.")
                
                Spacer(Modifier.height(32.dp))

                // Action Button
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Verified Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(icon: ImageVector, title: String, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EvidenceItem(source: String, title: String, quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = source,
                    color = AccentBlueLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = quote,
                color = TextSecondary,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ActionItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}
