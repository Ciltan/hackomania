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

    var showPopup by remember { mutableStateOf(false) }
    var showChatInput by remember { mutableStateOf(false) }
    var chatQuestion by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf(initialUrl ?: "https://www.google.com") }
    var hasAnalyzedCurrentPage by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Show popup only once analysis is DONE
    LaunchedEffect(analysisState) {
        if ((analysisState is AnalysisState.Success || analysisState is AnalysisState.Error) && hasAnalyzedCurrentPage) {
            showPopup = true
        }
    }

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
            // Browser top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .statusBarsPadding(),
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
                            text = currentUrl,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                                    
                                    // Skip analysis for search engines, roots, and common landing pages
                                    if (isSkippableUrl(newUrl)) {
                                        hasAnalyzedCurrentPage = false
                                        showPopup = false
                                        return
                                    }

                                    hasAnalyzedCurrentPage = true
                                    showPopup = false
                                    showChatInput = false
                                    chatQuestion = ""
                                    viewModel.analyze(newUrl)
                                }
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

        // Credibility Popup
        AnimatedVisibility(
            visible = showPopup && (analysisState is AnalysisState.Loading || analysisState is AnalysisState.Success || analysisState is AnalysisState.Error),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 12.dp),
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (analysisState is AnalysisState.Loading) AccentBlue.copy(alpha = 0.1f) else credColor.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (analysisState is AnalysisState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = AccentBlue, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                            } else {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = credColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(credLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = credColor)
                            }
                        }
                    }

                    if (analysisState !is AnalysisState.Loading) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(credSummary, fontSize = 11.sp, color = TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onViewAnalysis() },
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("VIEW DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
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
        
        // 1. Skip all main search engine domains and their search result paths
        val searchEngines = listOf("google.", "bing.com", "yahoo.com", "duckduckgo.com", "baidu.com", "yandex.")
        if (searchEngines.any { host.contains(it) }) {
            // Skip root pages or search result pages
            if (path == "/" || path.isEmpty() || path.startsWith("/search") || path.startsWith("/results")) {
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
        if (host.contains("accounts.google") || host.contains("play.google.com")) return true
        
    } catch (e: Exception) {
        // If URI is malformed, just check string contains as fallback
        return lower.contains("google.com/search") || lower.contains("bing.com/search")
    }

    return false
}
