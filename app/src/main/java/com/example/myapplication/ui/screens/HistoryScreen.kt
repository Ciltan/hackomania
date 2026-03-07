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
import com.example.myapplication.model.CredibilityLevel
import com.example.myapplication.model.MockData
import com.example.myapplication.model.RecentAnalysis
import com.example.myapplication.ui.theme.*

@Composable
fun HistoryScreen(
    onItemClick: (RecentAnalysis) -> Unit
) {
    val allHistory = remember {
        MockData.recentAnalyses + listOf(
            RecentAnalysis("4", "Singapore hawker centre food safety...", "2d ago", 78, CredibilityLevel.MEDIUM),
            RecentAnalysis("5", "CPF changes for 2025...", "3d ago", 94, CredibilityLevel.HIGH),
            RecentAnalysis("6", "Viral: Free iPhone giveaway by telco...", "4d ago", 5, CredibilityLevel.LOW),
            RecentAnalysis("7", "HDB BTO launch delayed to Q3...", "5d ago", 81, CredibilityLevel.HIGH)
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredHistory = remember(selectedFilter) {
        if (selectedFilter == "All") allHistory
        else allHistory.filter { it.credibilityLevel.name.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Spacer(Modifier.statusBarsPadding())

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "History",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = TextSecondary)
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "High", "Medium", "Low")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AccentBlue else SurfaceVariantDark)
                        .border(1.dp, if (isSelected) AccentBlue else CardBorderDark, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        filter,
                        fontSize = 13.sp,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            if (filteredHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history found for $selectedFilter", color = TextTertiary)
                }
            } else {
                filteredHistory.forEachIndexed { i, item ->
                    HistoryItemCard(item = item, onClick = { onItemClick(item) })
                    if (i < filteredHistory.lastIndex) Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HistoryItemCard(item: RecentAnalysis, onClick: () -> Unit) {
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Text("${item.credibilityScore}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = badgeColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.sp, color = TextPrimary, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(item.timeAgo, fontSize = 11.sp, color = TextTertiary)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badgeLabel, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
    }
}
