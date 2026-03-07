package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CredibilityLevel
import com.example.myapplication.model.MockData
import com.example.myapplication.model.RecentAnalysis
import com.example.myapplication.ui.theme.*

@Composable
fun HomeScreen(
    onCheckCredibility: (String) -> Unit,
    onRecentItemClick: (String) -> Unit,
    onNavigateUploadScreenshot: () -> Unit = {},
    onNavigateVerifyVideo: () -> Unit = {},
    onNavigateAnalyzeMessage: () -> Unit = {},
    onNavigateEmergencyHub: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }

    // Pulsing animation for trending dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Status bar spacer
        Spacer(Modifier.statusBarsPadding())

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Fact-Checker",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hub",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Trending Alert Banner
        TrendingAlertBanner(pulse = pulse)

        Spacer(Modifier.height(24.dp))

        // Check Credibility Section
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Check Credibility",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // Input field
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Paste text, URL or claim to verify...",
                        color = TextTertiary,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = AccentBlue,
                    unfocusedLeadingIconColor = TextTertiary
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (inputText.isNotBlank()) {
                        focusManager.clearFocus()
                        onCheckCredibility(inputText)
                    }
                }),
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Analyze Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (inputText.isNotBlank()) onCheckCredibility(inputText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Analyze Now",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

<<<<<<< HEAD
        Spacer(Modifier.height(24.dp))

        // Actions Section
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "More Options",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                title = "Upload Screenshot",
                icon = Icons.Filled.Image,
                onClick = onNavigateUploadScreenshot
            )
            Spacer(Modifier.height(10.dp))
            FeatureCard(
                title = "Verify Video",
                icon = Icons.Filled.Videocam,
                onClick = onNavigateVerifyVideo
            )
            Spacer(Modifier.height(10.dp))
            FeatureCard(
                title = "Analyze Message",
                icon = Icons.Filled.Message,
                onClick = onNavigateAnalyzeMessage
            )
            Spacer(Modifier.height(10.dp))
            FeatureCard(
                title = "Emergency Situations",
                icon = Icons.Filled.Warning,
                onClick = onNavigateEmergencyHub
            )
        }

=======
>>>>>>> cb8d8e4d3b3b3f38bb070ec3c11d6eb2a74d73d8
        Spacer(Modifier.height(28.dp))

        // Recent Analysis Section
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentBlue
                )
            }
            Spacer(Modifier.height(12.dp))

            MockData.recentAnalyses.forEach { item ->
                RecentAnalysisCard(
                    item = item,
                    onClick = { onRecentItemClick(item.id) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // How It Works section
        Spacer(Modifier.height(24.dp))
        HowItWorksSection()

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TrendingAlertBanner(pulse: Float) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TrendingBannerBg)
            .border(1.dp, TrendingBannerBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .drawBehind {
                            drawCircle(
                                color = TrendingPurple.copy(alpha = 0.3f),
                                radius = size.minDimension / 2 * pulse
                            )
                            drawCircle(color = TrendingPurple)
                        }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TRENDING ALERT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrendingPurple,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "• Singapore",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Viral misinformation alert: Local subsidy scams circulating via WhatsApp.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { }
            ) {
                Text(
                    text = "Details",
                    fontSize = 13.sp,
                    color = TrendingPurple,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TrendingPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentAnalysisCard(
    item: RecentAnalysis,
    onClick: () -> Unit
) {
    val (badgeColor, badgeBg, badgeLabel) = when (item.credibilityLevel) {
        CredibilityLevel.HIGH -> Triple(SuccessGreen, SuccessContainer, "High")
        CredibilityLevel.MEDIUM -> Triple(WarningOrange, WarningContainer, "Medium")
        CredibilityLevel.LOW -> Triple(DangerRed, DangerContainer, "Low")
        CredibilityLevel.UNVERIFIED -> Triple(TextSecondary, SurfaceVariantDark, "Unknown")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Credibility dot indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${item.credibilityScore}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Analyzed ${item.timeAgo}",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badgeLabel, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HowItWorksSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "How It Works",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        val steps = listOf(
            Triple(Icons.Filled.ContentPaste, "Submit", "Paste text, URL, or a claim you want to verify"),
            Triple(Icons.Filled.ManageSearch, "Analyze", "Our AI cross-references with trusted Singapore sources"),
            Triple(Icons.Filled.FactCheck, "Results", "Get a credibility score with source citations")
        )
        steps.forEach { (icon, title, desc) ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                    Text(desc, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
<<<<<<< HEAD

@Composable
fun FeatureCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(24.dp)
        )
    }
}
=======
>>>>>>> cb8d8e4d3b3b3f38bb070ec3c11d6eb2a74d73d8
