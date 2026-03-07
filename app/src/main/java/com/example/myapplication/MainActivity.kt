package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.AnalysisResult
import com.example.myapplication.model.MockData
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.components.FactCheckerBottomNav
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FactCheckerApp()
            }
        }
    }
}

@Composable
fun FactCheckerApp() {
    // Current main tab
    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }

    // Sub-navigation states
    var currentResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var showBrowser by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

    // Whether bottom nav is visible (hidden on browser/results detail screens)
    val showBottomNav = !showBrowser && !showResults

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomNav) {
                FactCheckerBottomNav(
                    currentRoute = currentTab.route,
                    onNavigate = { screen ->
                        showResults = false
                        showBrowser = false
                        currentTab = screen
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(bottom = if (showBottomNav) paddingValues.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f))) {
            // Results detail screen
            AnimatedVisibility(
                visible = showResults,
                enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            ) {
                currentResult?.let { result ->
                    ResultsScreen(
                        result = result,
                        onBack = { showResults = false },
                        onViewSource = {
                            showResults = false
                            showBrowser = true
                        }
                    )
                }
            }

            // Browser screen
            AnimatedVisibility(
                visible = showBrowser,
                enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            ) {
                BrowserScreen(
                    onBack = { showBrowser = false },
                    onCheckCredibility = {
                        showBrowser = false
                        currentResult = MockData.lowCredibilityResult
                        showResults = true
                    }
                )
            }

            // Main tab screens
            if (!showResults && !showBrowser) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "tabTransition"
                ) { tab ->
                    when (tab) {
                        Screen.Home -> HomeScreen(
                            onCheckCredibility = { text ->
                                val result = MockData.analyzeText(text)
                                currentResult = result
                                showResults = true
                            },
                            onRecentItemClick = { id ->
                                val result = when (id) {
                                    "1" -> MockData.highCredibilityResult
                                    else -> MockData.highCredibilityResult
                                }
                                currentResult = result
                                showResults = true
                            },
                            onNavigateUploadScreenshot = { currentTab = Screen.UploadScreenshot },
                            onNavigateVerifyVideo = { currentTab = Screen.VerifyVideo },
                            onNavigateAnalyzeMessage = { currentTab = Screen.AnalyzeMessage },
                            onNavigateEmergencyHub = { currentTab = Screen.EmergencyHub }
                        )
                        Screen.History -> HistoryScreen(
                            onItemClick = { _ ->
                                currentResult = MockData.highCredibilityResult
                                showResults = true
                            }
                        )
                        Screen.Explore -> ExploreScreen(
                            onTopicClick = { _ ->
                                showBrowser = true
                            }
                        )
                        Screen.Settings -> SettingsScreen()
                        Screen.UploadScreenshot -> UploadScreenshotScreen(
                            onBack = { currentTab = Screen.Home },
                            onAnalyze = { 
                                currentResult = MockData.lowCredibilityResult
                                showResults = true
                            }
                        )
                        Screen.VerifyVideo -> VerifyVideoScreen(onBack = { currentTab = Screen.Home })
                        Screen.AnalyzeMessage -> AnalyzeMessageScreen(
                            onBack = { currentTab = Screen.Home },
                            onAnalyze = { currentTab = Screen.ScamAnalysis }
                        )
                        Screen.ScamAnalysis -> ScamAnalysisScreen(onBack = { currentTab = Screen.AnalyzeMessage })
                        Screen.EmergencyHub -> EmergencyHubScreen(
                            onBack = { currentTab = Screen.Home },
                            onRumorClick = { currentTab = Screen.FactCheckDetail }
                        )
                        Screen.FactCheckDetail -> FactCheckDetailScreen(onBack = { currentTab = Screen.EmergencyHub })
                        else -> HomeScreen(
                            onCheckCredibility = { text ->
                                val result = MockData.analyzeText(text)
                                currentResult = result
                                showResults = true
                            },
                            onRecentItemClick = { _ ->
                                currentResult = MockData.highCredibilityResult
                                showResults = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(com.example.myapplication.ui.theme.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Text(title, color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onBack) { androidx.compose.material3.Text("Go Back") }
    }
}