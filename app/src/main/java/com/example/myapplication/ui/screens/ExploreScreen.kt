package com.example.myapplication.ui.screens

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AnalysisState
import com.example.myapplication.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: AnalysisViewModel, 
    initialUrl: String? = null,
    isOverlay: Boolean = false,
    onOverlayBack: () -> Unit = {},
    onViewAnalysis: () -> Unit = {}
) {
    val analysisState by viewModel.state.collectAsStateWithLifecycle()
    val chatAnswer by viewModel.chatAnswer.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val translatedText by viewModel.translatedText.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()

    var showPopup by remember { mutableStateOf(false) }
    var showChatInput by remember { mutableStateOf(false) }
    var chatQuestion by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf(initialUrl ?: "https://www.google.com") }
    var hasAnalyzedCurrentPage by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Translate state
    var showTranslateBanner by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("简体中文") }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val languages = listOf("简体中文", "Bahasa Melayu", "Tamil", "日本語", "한국어", "Español", "Français")

    // Show popup when analysis is Loading, Success, or Error
    LaunchedEffect(analysisState) {
        if ((analysisState is AnalysisState.Loading || analysisState is AnalysisState.Success || analysisState is AnalysisState.Error) && hasAnalyzedCurrentPage) {
            showPopup = true
        }
    }

    // Derive credibility info from analysis result
    val credLabel: String
    val credColor: Color
    val credSummary: String

    when (val state = analysisState) {
        is AnalysisState.Success -> {
            val result = state.result
            credLabel = when {
                result.credibilityScore >= 70 -> "Reliable Content"
                result.credibilityScore >= 40 -> "Unverified Content"
                else -> "Misleading Detected"
            }
            credColor = when {
                result.credibilityScore >= 70 -> SuccessGreen
                result.credibilityScore >= 40 -> WarningOrange
                else -> DangerRed
            }
            credSummary = result.credibilitySummary
        }
        is AnalysisState.Error -> {
            credLabel = "Analysis Error"
            credColor = DangerRed
            credSummary = state.message
        }
        else -> {
            credLabel = ""
            credColor = TextTertiary
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

            // Browser top bar (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOverlay) {
                    IconButton(onClick = onOverlayBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = currentUrl.ifEmpty { "Loading..." },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Translate banner (shows when translate is active)
            AnimatedVisibility(visible = showTranslateBanner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Translate, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    if (isTranslating) {
                        Text("Translating...", fontSize = 12.sp, color = AccentBlueLight)
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentBlue, strokeWidth = 2.dp)
                    } else {
                        Text("Translated to: $selectedLanguage", fontSize = 12.sp, color = AccentBlueLight)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showLanguagePicker = true }) {
                        Text("Change", fontSize = 12.sp, color = DangerRed, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { 
                        showTranslateBanner = false 
                        val js = """
                            (function() {
                                var domain = window.location.hostname;
                                document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/';
                                document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=' + domain;
                                document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=.' + domain;
                                window.location.reload();
                            })();
                        """.trimIndent()
                        webViewRef?.evaluateJavascript(js, null)
                    }) {
                        Text("✕", fontSize = 14.sp, color = TextTertiary)
                    }
                }
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val newUrl = url ?: return
                                
                                // Update current URL state for the address bar
                                if (newUrl != currentUrl) {
                                    currentUrl = newUrl
                                    
                                    if (isSkippableUrl(newUrl)) {
                                        hasAnalyzedCurrentPage = false
                                        showPopup = false
                                        return
                                    }

                                    hasAnalyzedCurrentPage = true
                                    showPopup = true
                                    showChatInput = false
                                    chatQuestion = ""
                                    viewModel.analyze(newUrl)
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadUrl(currentUrl)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ====== SMALL TOP-RIGHT CREDIBILITY POPUP ======
        AnimatedVisibility(
            visible = showPopup && (analysisState is AnalysisState.Loading || analysisState is AnalysisState.Success || analysisState is AnalysisState.Error),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 12.dp)
                .statusBarsPadding(),
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .shadow(16.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            ) {
                Column {
                    // Colored header strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (analysisState is AnalysisState.Loading) AccentBlue.copy(alpha = 0.15f)
                                else credColor.copy(alpha = 0.15f)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (analysisState is AnalysisState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = AccentBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing Page...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = credColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = credLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = credColor
                                )
                            }
                        }
                    }

                    if (analysisState !is AnalysisState.Loading) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Summary text
                            Text(
                                text = credSummary,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                maxLines = 4
                            )

                            Spacer(Modifier.height(10.dp))

                            // VIEW ANALYSIS button
                            Button(
                                onClick = {
                                    showPopup = false
                                    onViewAnalysis()
                                },
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("VIEW ANALYSIS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(8.dp))

                            // Secondary actions row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Translate button
                                OutlinedButton(
                                    onClick = {
                                        showTranslateBanner = true
                                        showPopup = false
                                        
                                        val tlCode = when(selectedLanguage) {
                                            "简体中文", "Chinese" -> "zh-CN"
                                            "Bahasa Melayu", "Malay" -> "ms"
                                            "Tamil" -> "ta"
                                            "日本語", "Japanese" -> "ja"
                                            "한국어", "Korean" -> "ko"
                                            "Español", "Spanish" -> "es"
                                            "Français", "French" -> "fr"
                                            else -> "zh-CN"
                                        }
                                        
                                        try {
                                            val uri = java.net.URI(currentUrl)
                                            val host = uri.host ?: ""
                                            val scheme = uri.scheme ?: "https"
                                            val pathAndQuery = currentUrl.substring(currentUrl.indexOf(host) + host.length)
                                            val translatedHost = host.replace("-", "--").replace(".", "-") + ".translate.goog"
                                            val tbUrl = "$scheme://$translatedHost$pathAndQuery${if (pathAndQuery.contains("?")) "&" else "?"}_x_tr_sl=auto&_x_tr_tl=$tlCode&_x_tr_hl=en&_x_tr_pto=wapp"
                                            webViewRef?.loadUrl(tbUrl)
                                        } catch(e: Exception) {
                                            // Ignore malformed URLs
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, CardBorderDark),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Outlined.Translate, contentDescription = null, modifier = Modifier.size(12.dp), tint = AccentBlueLight)
                                    Spacer(Modifier.width(3.dp))
                                    Text("Translate", fontSize = 10.sp, color = TextSecondary)
                                }

                                // Ask AI button
                                OutlinedButton(
                                    onClick = { showChatInput = !showChatInput },
                                    modifier = Modifier.weight(1f).height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, CardBorderDark),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Outlined.QuestionAnswer, contentDescription = null, modifier = Modifier.size(12.dp), tint = AccentBlueLight)
                                    Spacer(Modifier.width(3.dp))
                                    Text("Ask AI", fontSize = 10.sp, color = TextSecondary)
                                }
                            }

                            // Chat input section (expandable)
                            AnimatedVisibility(visible = showChatInput) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    OutlinedTextField(
                                        value = chatQuestion,
                                        onValueChange = { chatQuestion = it },
                                        placeholder = { Text("Ask about this...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp),
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
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
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
                                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = AccentBlue, strokeWidth = 2.dp)
                                                } else {
                                                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = AccentBlue, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        maxLines = 2
                                    )

                                    // Chat answer
                                    if (chatAnswer != null) {
                                        Spacer(Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SurfaceVariantDark)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = chatAnswer!!,
                                                fontSize = 10.sp,
                                                color = TextPrimary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Dismiss link
                            Spacer(Modifier.height(4.dp))
                            TextButton(
                                onClick = { showPopup = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Dismiss", fontSize = 10.sp, color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }

        // ====== FLOATING RE-OPEN BUTTON ======
        if (!showPopup && hasAnalyzedCurrentPage && (analysisState is AnalysisState.Success || analysisState is AnalysisState.Error)) {
            FloatingActionButton(
                onClick = { showPopup = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .size(48.dp),
                containerColor = credColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = "Show Analysis", modifier = Modifier.size(20.dp))
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
                                
                                val tlCode = when(lang) {
                                    "简体中文", "Chinese" -> "zh-CN"
                                    "Bahasa Melayu", "Malay" -> "ms"
                                    "Tamil" -> "ta"
                                    "日本語", "Japanese" -> "ja"
                                    "한국어", "Korean" -> "ko"
                                    "Español", "Spanish" -> "es"
                                    "Français", "French" -> "fr"
                                    else -> "zh-CN"
                                }
                                
                                try {
                                    val uri = java.net.URI(currentUrl)
                                    val host = uri.host ?: ""
                                    val scheme = uri.scheme ?: "https"
                                    val pathAndQuery = currentUrl.substring(currentUrl.indexOf(host) + host.length)
                                    val translatedHost = host.replace("-", "--").replace(".", "-") + ".translate.goog"
                                    val tbUrl = "$scheme://$translatedHost$pathAndQuery${if (pathAndQuery.contains("?")) "&" else "?"}_x_tr_sl=auto&_x_tr_tl=$tlCode&_x_tr_hl=en&_x_tr_pto=wapp"
                                    webViewRef?.loadUrl(tbUrl)
                                } catch(e: Exception) {
                                    // Ignore
                                }
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

/**
 * Robust filter to prevent scanning search engines, homepages, and non-article content.
 */
private fun isSkippableUrl(url: String): Boolean {
    val lower = url.lowercase()
    if (lower == "about:blank" || lower.isEmpty()) return true
    
    try {
        val uri = java.net.URI(url)
        val host = uri.host ?: ""
        val path = uri.path ?: ""
        
        // 0. Skip Google Translate proxy visits to avoid re-analyzing the same content
        if (host.endsWith("translate.goog")) return true

        // 1. Skip all main search engine domains and their search result paths
        val searchEngines = listOf("google.", "bing.com", "yahoo.com", "duckduckgo.com", "baidu.com", "yandex.")
        if (searchEngines.any { host.contains(it) }) {
            // Skip root pages or search result pages
            if (path == "/" || path.isEmpty() || (path.startsWith("/search") && !url.contains("url?q="))) {
                return true
            }
        }

        // 2. Skip common social media root/feed pages (only analyze specific links/posts)
        val socialRoots = listOf("facebook.com", "twitter.com", "x.com", "instagram.com", "tiktok.com", "reddit.com")
        if (socialRoots.any { host.endsWith(it) }) {
            if (path == "/" || path.isEmpty() || path.length < 5) {
                return true
            }
        }

        // 3. Skip system pages
        if (host.contains("accounts.google") || host.contains("play.google.com") || host.contains("outlook.") || host.contains("linkedin.com/login")) return true
        
    } catch (e: Exception) {
        // If URI is malformed, just check string contains as fallback
        return lower.contains("google.com/search") || lower.contains("bing.com/search")
    }

    return false
}
