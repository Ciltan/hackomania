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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ArticleContent
import com.example.myapplication.model.MockData
import com.example.myapplication.model.SourceReputation
import com.example.myapplication.ui.theme.*

@Composable
fun BrowserScreen(
    article: ArticleContent = MockData.articleContent,
    onBack: () -> Unit,
    onCheckCredibility: (String) -> Unit
) {
    val warningColor: Color
    val warningBg: Color
    val reputationLabel: String

    when (article.sourceReputation) {
        SourceReputation.QUESTIONABLE -> {
            warningColor = DangerRed
            warningBg = DangerContainer
            reputationLabel = "Questionable Source"
        }
        SourceReputation.UNKNOWN -> {
            warningColor = WarningOrange
            warningBg = WarningContainer
            reputationLabel = "Unknown Source"
        }
        SourceReputation.TRUSTED -> {
            warningColor = SuccessGreen
            warningBg = SuccessContainer
            reputationLabel = "Trusted Source"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Spacer(Modifier.statusBarsPadding())

        // Browser top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = article.url,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }

        HorizontalDivider(color = CardBorderDark, thickness = 0.5.dp)

        // Credibility Warning Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(warningBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = warningColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = reputationLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = warningColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.warningMessage,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onCheckCredibility(article.url) },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Check This Article", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Article content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(warningBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = reputationLabel, fontSize = 10.sp, color = warningColor, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                Text("Unknown Author", fontSize = 11.sp, color = TextTertiary)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = CardBorderDark)
            Spacer(Modifier.height(16.dp))

            article.body.split("\n\n").forEach { paragraph ->
                if (paragraph.isNotBlank()) {
                    Text(text = paragraph.trim(), fontSize = 15.sp, color = TextPrimary, lineHeight = 24.sp)
                    Spacer(Modifier.height(14.dp))
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
