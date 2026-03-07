package com.example.myapplication.ui.screens

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyHubScreen(onBack: () -> Unit, onRumorClick: () -> Unit) {
    var isEmergencyModeActive by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Emergency Hub", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", modifier = Modifier.size(22.dp))
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            EmergencyModeCard(
                isEmergencyModeActive = isEmergencyModeActive,
                onCheckedChange = { isEmergencyModeActive = it }
            )
            
            if (isEmergencyModeActive) {
            
            Spacer(Modifier.height(24.dp))
            
            SectionHeader(
                title = "Verified Updates",
                badgeText = "Official",
                badgeColor = SuccessGreen,
                badgeBg = SuccessContainer,
                badgeIcon = Icons.Filled.CheckCircle
            )
            Spacer(Modifier.height(16.dp))
            
            VerifiedUpdateCard(
                title = "Sector 7 Evacuation Order",
                time = "4m ago",
                description = "Mandatory evacuation for residents within 2 miles of the riverbank. High water levels detected by AI sensor networks.",
                source = "METROPOLITAN EMERGENCY SVCS",
                sourceIcon = Icons.Filled.Apartment,
                sourceIconTint = TextSecondary,
                icon = Icons.Filled.Campaign,
                iconTint = AccentBlueDark
            )
            Spacer(Modifier.height(12.dp))
            VerifiedUpdateCard(
                title = "Potable Water Stations Open",
                time = "22m ago",
                description = "Three new water distribution points are now active at Central Mall, High Street, and West Park.",
                source = "RED CROSS LOCAL",
                sourceIcon = Icons.Filled.AddBox,
                sourceIconTint = DangerRed,
                icon = Icons.Filled.WaterDrop,
                iconTint = AccentBlue
            )
            
            Spacer(Modifier.height(24.dp))
            
            SectionHeader(
                title = "Rumor Alerts",
                badgeText = "Monitoring",
                badgeColor = WarningOrange,
                badgeBg = WarningContainer,
                badgeIcon = Icons.Filled.HourglassBottom
            )
            Spacer(Modifier.height(16.dp))
            
            RumorAlertCard(
                rumor = "\"Main power grid failure across the city expected at midnight.\"",
                statusBadge = "DEBUNKED",
                statusBadgeColor = DangerRed,
                statusBadgeBg = DangerContainer,
                statusTitle = "AI FACT-CHECK",
                statusDesc = "Grid operators confirm backup systems are operational. The viral video showing a blackout is from a different region dated 2021.",
                platform = "Trending on X/Twitter",
                icon = Icons.Filled.Cancel,
                iconTint = DangerRed,
                onClick = onRumorClick
            )
            Spacer(Modifier.height(12.dp))
            RumorAlertCard(
                rumor = "\"Rumors of Bridge 4 closure due to structural cracks.\"",
                statusBadge = "INVESTIGATING",
                statusBadgeColor = WarningOrange,
                statusBadgeBg = WarningContainer,
                statusTitle = "AI STATUS",
                statusDesc = "Cross-referencing traffic cam data and official structural reports. No official closures yet. Awaiting city engineer confirmation.",
                platform = "Trending on Telegram",
                icon = Icons.Filled.HourglassEmpty,
                iconTint = WarningOrange,
                onClick = onRumorClick
            )
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmergencyModeCard(isEmergencyModeActive: Boolean, onCheckedChange: (Boolean) -> Unit) {
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DangerRed))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Emergency Mode Active",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isEmergencyModeActive,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentBlue
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Real-time AI fact-checking is currently prioritized for your region.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, badgeText: String, badgeColor: Color, badgeBg: Color, badgeIcon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(badgeBg)
                .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(badgeIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VerifiedUpdateCard(
    title: String,
    time: String,
    description: String,
    source: String,
    sourceIcon: ImageVector,
    sourceIconTint: Color,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = time,
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(sourceIcon, contentDescription = null, tint = sourceIconTint, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = source,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun RumorAlertCard(
    rumor: String,
    statusBadge: String,
    statusBadgeColor: Color,
    statusBadgeBg: Color,
    statusTitle: String,
    statusDesc: String,
    platform: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusBadgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusBadge,
                    color = statusBadgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = platform,
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = rumor,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariantDark)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = statusTitle,
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = statusDesc,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
    }
}
