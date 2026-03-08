package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import com.example.myapplication.model.*
import com.example.myapplication.ui.theme.*

@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onBack: () -> Unit,
    onViewSource: (String) -> Unit
) {
    val accentColor = when (result.credibilityLevel) {
        CredibilityLevel.HIGH -> SuccessGreen
        CredibilityLevel.MEDIUM -> WarningOrange
        CredibilityLevel.LOW -> DangerRed
        CredibilityLevel.UNVERIFIED -> TextSecondary
    }
    val bgColor = when (result.credibilityLevel) {
        CredibilityLevel.HIGH -> SuccessContainer
        CredibilityLevel.MEDIUM -> WarningContainer
        CredibilityLevel.LOW -> DangerContainer
        CredibilityLevel.UNVERIFIED -> SurfaceVariantDark
    }
    val levelLabel = when (result.credibilityLevel) {
        CredibilityLevel.HIGH -> "High Credibility"
        CredibilityLevel.MEDIUM -> "Medium Credibility"
        CredibilityLevel.LOW -> "Low Credibility"
        CredibilityLevel.UNVERIFIED -> "Unverified"
    }
    val levelIcon = when (result.credibilityLevel) {
        CredibilityLevel.HIGH -> Icons.Filled.CheckCircle
        CredibilityLevel.MEDIUM -> Icons.Filled.Warning
        CredibilityLevel.LOW -> Icons.Filled.Cancel
        CredibilityLevel.UNVERIFIED -> Icons.Filled.HelpOutline
    }

    val animatedScore by animateFloatAsState(
        targetValue = result.credibilityScore.toFloat(),
        animationSpec = tween(1200, easing = EaseOut),
        label = "score"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.statusBarsPadding())

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Analysis Results",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextSecondary)
            }
        }

        // Credibility Score Hero Card
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.15f), bgColor)
                    )
                )
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center) {
                    ScoreArcCanvas(
                        score = animatedScore / 100f,
                        color = accentColor,
                        modifier = Modifier.size(120.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedScore.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(text = "/ 100", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(levelIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(text = levelLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = result.credibilitySummary,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Original Submission
        ResultSectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Article, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Original Submission", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = result.originalText, fontSize = 14.sp, color = TextPrimary, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(text = "Captured: ${result.capturedTimeAgo}", fontSize = 11.sp, color = TextTertiary)
        }

        Spacer(Modifier.height(12.dp))

        // Claims Breakdown
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Claims Breakdown", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            result.claims.forEachIndexed { index, claim ->
                ClaimCard(claim = claim, onViewSource = onViewSource)
                if (index < result.claims.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Context Summary
        ResultSectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Context Summary", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = result.contextSummary, fontSize = 14.sp, color = TextPrimary, lineHeight = 22.sp)
        }

        if (result.sourceUrl != null) {
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedButton(
                    onClick = { onViewSource(result.sourceUrl) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                ) {
                    Icon(Icons.Outlined.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Original Source", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ScoreArcCanvas(score: Float, color: Color, modifier: Modifier = Modifier) {
    val trackColor = color.copy(alpha = 0.15f)
    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val startAngle = 135f
        val sweepAngle = 270f
        drawArc(color = trackColor, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle * score, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

@Composable
private fun ClaimCard(claim: Claim, onViewSource: (String) -> Unit) {
    val verdictColor = when (claim.verdict) {
        ClaimVerdict.VERIFIED -> SuccessGreen
        ClaimVerdict.MISLEADING -> WarningOrange
        ClaimVerdict.FALSE -> DangerRed
        ClaimVerdict.UNVERIFIABLE -> TextSecondary
    }
    val verdictBg = when (claim.verdict) {
        ClaimVerdict.VERIFIED -> SuccessContainer
        ClaimVerdict.MISLEADING -> WarningContainer
        ClaimVerdict.FALSE -> DangerContainer
        ClaimVerdict.UNVERIFIABLE -> SurfaceVariantDark
    }
    val verdictLabel = when (claim.verdict) {
        ClaimVerdict.VERIFIED -> "Verified"
        ClaimVerdict.MISLEADING -> "Misleading"
        ClaimVerdict.FALSE -> "False"
        ClaimVerdict.UNVERIFIABLE -> "Unverifiable"
    }
    val verdictIcon = when (claim.verdict) {
        ClaimVerdict.VERIFIED -> Icons.Filled.CheckCircle
        ClaimVerdict.MISLEADING -> Icons.Filled.Warning
        ClaimVerdict.FALSE -> Icons.Filled.Cancel
        ClaimVerdict.UNVERIFIABLE -> Icons.Filled.HelpOutline
    }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(verdictBg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(verdictIcon, contentDescription = null, tint = verdictColor, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(verdictLabel, fontSize = 11.sp, color = verdictColor, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = claim.text, fontSize = 13.sp, color = TextPrimary, lineHeight = 20.sp)
        if (claim.sources.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = CardBorderDark)
            Spacer(Modifier.height(8.dp))
            Text("Sources", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            claim.sources.forEach { source ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { 
                            if (source.url.isNotEmpty()) {
                                onViewSource(source.url)
                            }
                        }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(text = source.title, fontSize = 12.sp, color = AccentBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun ResultSectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content
    )
}
