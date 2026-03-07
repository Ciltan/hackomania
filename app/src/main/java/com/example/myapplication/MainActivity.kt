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
import androidx.compose.ui.Modifier
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
                            }
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