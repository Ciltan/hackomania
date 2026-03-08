package com.example.myapplication.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ArticleContent
import com.example.myapplication.model.MockData
import com.example.myapplication.model.SourceReputation
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AnalysisState
import com.example.myapplication.viewmodel.AnalysisViewModel

@Composable
fun BrowserScreen(
    article: ArticleContent = MockData.articleContent,
    onBack: () -> Unit,
    onCheckCredibility: (String) -> Unit,
    viewModel: AnalysisViewModel
) {
    // Auto-trigger analysis when the screen loads
    LaunchedEffect(article.url) {
        viewModel.analyze(article.url)
    }

    val analysisState by viewModel.state.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val chatAnswer by viewModel.chatAnswer.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var showPopup by remember { mutableStateOf(true) }
    var showChatInput by remember { mutableStateOf(false) }
    var chatQuestion by remember { mutableStateOf("") }

    // Translate state
    var isTranslateActive by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("简体中文") }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val languages = listOf("简体中文", "Bahasa Melayu", "Tamil", "日本語", "한국어", "Español", "Français")

    // Determine display text for article body
    val displayBody = if (isTranslateActive && translatedText != null) {
        translatedText!!
    } else {
        article.body
    }

    // Derive credibility info from analysis result
    val credLabel: String
    val credColor: Color
    val credBg: Color
    val credSummary: String

    when (analysisState) {
        is AnalysisState.Success -> {
            val result = (analysisState as AnalysisState.Success).result
            credLabel = when {
                result.credibilityScore >= 70 -> "Reliable Content"
                result.credibilityScore >= 40 -> "Unverified Content"
                else -> "Misleading Content Detected"
            }
            credColor = when {
                result.credibilityScore >= 70 -> SuccessGreen
                result.credibilityScore >= 40 -> WarningOrange
                else -> DangerRed
            }
            credBg = when {
                result.credibilityScore >= 70 -> SuccessContainer
                result.credibilityScore >= 40 -> WarningContainer
                else -> DangerContainer
            }
            credSummary = result.credibilitySummary
        }
        is AnalysisState.Loading -> {
            credLabel = "Analyzing..."
            credColor = AccentBlue
            credBg = SurfaceVariantDark
            credSummary = "Checking credibility of this article..."
        }
        is AnalysisState.Error -> {
            credLabel = "Analysis Unavailable"
            credColor = WarningOrange
            credBg = WarningContainer
            credSummary = (analysisState as AnalysisState.Error).message
        }
        else -> {
            credLabel = "Checking..."
            credColor = TextTertiary
            credBg = SurfaceVariantDark
            credSummary = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.statusBarsPadding())

            // Browser top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Translate icon (left of URL bar)
                if (isTranslateActive) {
                    IconButton(
                        onClick = { isTranslateActive = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("文", fontSize = 18.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }

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
                    Icon(Icons.Filled.Refresh, contentDescription = "Reload", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = CardBorderDark, thickness = 0.5.dp)

            // Translate banner (shows when translate is active)
            AnimatedVisibility(visible = isTranslateActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Translate, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("正在翻译为：$selectedLanguage", fontSize = 12.sp, color = AccentBlueLight)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showLanguagePicker = true }) {
                        Text("更改", fontSize = 12.sp, color = DangerRed, fontWeight = FontWeight.SemiBold)
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
                    Text("By ${article.warningMessage.take(20)}", fontSize = 11.sp, color = TextTertiary)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = CardBorderDark)
                Spacer(Modifier.height(16.dp))

                displayBody.split("\n\n").forEach { paragraph ->
                    if (paragraph.isNotBlank()) {
                        Text(text = paragraph.trim(), fontSize = 15.sp, color = TextPrimary, lineHeight = 24.sp)
                        Spacer(Modifier.height(14.dp))
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        // ====== CREDIBILITY POPUP OVERLAY ======
        AnimatedVisibility(
            visible = showPopup,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .shadow(24.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Warning icon + title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = credColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = credLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Summary text
                    Text(
                        text = credSummary,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    // Loading indicator
                    if (analysisState is AnalysisState.Loading) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = AccentBlue,
                            trackColor = SurfaceVariantDark
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // VIEW ANALYSIS button
                        Button(
                            onClick = {
                                showPopup = false
                                onCheckCredibility(article.url)
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("VIEW ANALYSIS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Secondary actions: Translate + Ask AI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Translate button
                        OutlinedButton(
                            onClick = {
                                isTranslateActive = true
                                showPopup = false
                                viewModel.translateArticle(article.body, selectedLanguage)
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorderDark),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Outlined.Translate, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentBlueLight)
                            Spacer(Modifier.width(4.dp))
                            Text("Translate", fontSize = 11.sp, color = TextSecondary)
                        }

                        // Ask AI button
                        OutlinedButton(
                            onClick = { showChatInput = !showChatInput },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorderDark),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Outlined.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentBlueLight)
                            Spacer(Modifier.width(4.dp))
                            Text("Ask AI", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    // Chat input section (expandable)
                    AnimatedVisibility(visible = showChatInput) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = chatQuestion,
                                onValueChange = { chatQuestion = it },
                                placeholder = { Text("Ask about this analysis...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedContainerColor = SurfaceVariantDark,
                                    unfocusedContainerColor = SurfaceVariantDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedPlaceholderColor = TextTertiary,
                                    unfocusedPlaceholderColor = TextTertiary,
                                    cursorColor = AccentBlue
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (chatQuestion.isNotBlank() && analysisState is AnalysisState.Success) {
                                                val result = (analysisState as AnalysisState.Success).result
                                                viewModel.askFollowUp(
                                                    claim = result.originalText.take(500),
                                                    analysisSummary = result.credibilitySummary,
                                                    question = chatQuestion
                                                )
                                            }
                                        },
                                        enabled = !isChatLoading && chatQuestion.isNotBlank()
                                    ) {
                                        if (isChatLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = AccentBlue,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = AccentBlue, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                maxLines = 3
                            )

                            // Chat answer
                            if (chatAnswer != null) {
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantDark)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = chatAnswer!!,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Dismiss text
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showPopup = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Dismiss", fontSize = 12.sp, color = TextTertiary)
                    }
                }
            }
        }

        // ====== FLOATING RE-OPEN BUTTON (bottom-right) ======
        if (!showPopup) {
            FloatingActionButton(
                onClick = { showPopup = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                containerColor = credColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = "Show Analysis", modifier = Modifier.size(24.dp))
            }
        }
    }

    // Language picker dialog
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text("Select Language", color = TextPrimary) },
            text = {
                Column {
                    languages.forEach { lang ->
                        TextButton(
                            onClick = {
                                selectedLanguage = lang
                                showLanguagePicker = false
                                viewModel.translateArticle(article.body, lang)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = lang,
                                color = if (lang == selectedLanguage) AccentBlue else TextPrimary,
                                fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = CardDark,
            tonalElevation = 0.dp
        )
    }
}
