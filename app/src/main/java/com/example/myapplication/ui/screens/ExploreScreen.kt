package com.example.myapplication.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun ExploreScreen(
    onTopicClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Spacer(Modifier.statusBarsPadding())

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Explore",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search bar
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Search topics, claims...", fontSize = 14.sp, color = TextTertiary)
            }

            Spacer(Modifier.height(20.dp))

            // Trending Topics
            Text(
                "Trending Topics",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            val topics = listOf(
                Triple("🏥", "Health & Medicine", "234 analyses"),
                Triple("💰", "Finance & Subsidies", "189 analyses"),
                Triple("🏠", "Housing (HDB/BTO)", "156 analyses"),
                Triple("📱", "Social Media Scams", "298 analyses"),
                Triple("🌦️", "Weather Alerts", "87 analyses"),
                Triple("🚇", "Transport & MRT", "112 analyses")
            )

            topics.forEach { (emoji, title, count) ->
                TopicCard(emoji = emoji, title = title, count = count, onClick = { onTopicClick(title) })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))

            // Featured Debunks
            Text(
                "Recent Debunks",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            val debunks = listOf(
                Triple(DangerRed, "FALSE", "Viral message claiming CPF minimum sum doubled overnight"),
                Triple(WarningOrange, "MISLEADING", "Article about MRT fares increasing 40% is exaggerated"),
                Triple(DangerRed, "FALSE", "WhatsApp forward about free government laptops for seniors")
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                debunks.forEachIndexed { i, (color, verdict, text) ->
                    DebunkCard(color = color, verdict = verdict, text = text)
                    if (i < debunks.lastIndex) Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopicCard(emoji: String, title: String, count: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(count, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DebunkCard(color: androidx.compose.ui.graphics.Color, verdict: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(verdict, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f), lineHeight = 20.sp)
    }
}
