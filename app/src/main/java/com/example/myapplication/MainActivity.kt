package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.AnalysisResult
import com.example.myapplication.model.MockData
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.components.FactCheckerBottomNav
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AnalysisState
import com.example.myapplication.viewmodel.AnalysisViewModel

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
    val context = LocalContext.current
    val viewModel: AnalysisViewModel = viewModel()
    val analysisState by viewModel.state.collectAsStateWithLifecycle()

    // Current main tab
    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }

<<<<<<< HEAD
    // The result to show — can come from either the live API or history
=======
    // The result to show — can come from either the live API or mock history items
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
    var displayedResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var showBrowser by remember { mutableStateOf(false) }
    var browserUrl by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }

    val showBottomNav = !showBrowser && !showResults

    // When the ViewModel emits a Success, capture the result and show the screen
<<<<<<< HEAD
=======
    // BUT skip if the browser or Explore tab is active (they have their own popups)
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
    LaunchedEffect(analysisState) {
        val isExploreActive = currentTab == Screen.Explore && !showBrowser && !showResults
        when (val state = analysisState) {
            is AnalysisState.Success -> {
<<<<<<< HEAD
                // If we're in the Explore tab (WebView), the screen handles its own "Success" popup.
                // We only auto-navigate to the full ResultsScreen if we're on Home/History/etc.
=======
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                if (!showBrowser && !isExploreActive) {
                    displayedResult = state.result
                    showResults = true
                }
            }
            is AnalysisState.Error -> {
                if (!showBrowser && !isExploreActive) {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetState()
                }
            }
<<<<<<< HEAD
            else -> { /* Idle / Loading */ }
=======
            else -> { /* Idle / Loading — handled inline */ }
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomNav) {
                FactCheckerBottomNav(
                    currentRoute = currentTab.route,
                    onNavigate = { screen ->
                        currentTab = screen
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(
                bottom = if (showBottomNav) paddingValues.calculateBottomPadding()
                         else androidx.compose.ui.unit.Dp(0f)
            )
        ) {

<<<<<<< HEAD
            // Main tab screens
=======
            // Main tab screens — ALWAYS composed so WebView persists
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    Screen.Home -> HomeScreen(
                        isLoading = analysisState is AnalysisState.Loading,
                        onCheckCredibility = { input ->
<<<<<<< HEAD
                            viewModel.analyze(input)
                        },
                        onRecentItemClick = { id ->
                            // Local recent items fallback to mock until history is unified
                            val result = if (id == "1" || id == "2") MockData.highCredibilityResult else MockData.lowCredibilityResult
=======
                            // Call the real backend API
                            viewModel.analyze(input)
                        },
                        onRecentItemClick = { id ->
                            // Recent items still use mock data as placeholders
                            // until history is persisted in the backend DB
                            val result = when (id) {
                                "1" -> MockData.highCredibilityResult
                                "2" -> MockData.highCredibilityResult
                                else -> MockData.lowCredibilityResult
                            }
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                            displayedResult = result
                            showResults = true
                        },
                        onNavigateUploadScreenshot = { currentTab = Screen.UploadScreenshot },
                        onNavigateVerifyVideo = { currentTab = Screen.VerifyVideo },
                        onNavigateAnalyzeMessage = { currentTab = Screen.AnalyzeMessage },
                        onNavigateEmergencyHub = { currentTab = Screen.EmergencyHub }
                    )
                    Screen.History -> HistoryScreen(
<<<<<<< HEAD
                        viewModel = viewModel,
                        onItemClick = { result ->
                            displayedResult = result
=======
                        onItemClick = { _ ->
                            displayedResult = MockData.highCredibilityResult
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                            showResults = true
                        }
                    )
                    Screen.Explore -> ExploreScreen(
                        viewModel = viewModel,
                        onViewAnalysis = {
                            val state = viewModel.state.value
                            if (state is AnalysisState.Success) {
                                displayedResult = state.result
                                showResults = true
                            }
                        }
                    )
                    Screen.Settings -> SettingsScreen()
                    Screen.UploadScreenshot -> UploadScreenshotScreen(
                        onBack = { currentTab = Screen.Home },
                        onAnalyze = { url, uri ->
<<<<<<< HEAD
                            if (uri != null) viewModel.analyzeFile(context, uri)
                            else if (url != null) viewModel.analyze(url)
=======
                            if (uri != null) {
                                viewModel.analyzeFile(context, uri)
                            } else if (url != null) {
                                viewModel.analyze(url)
                            }
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                        },
                        isLoading = analysisState is AnalysisState.Loading
                    )
                    Screen.VerifyVideo -> VerifyVideoScreen(
                        onBack = { currentTab = Screen.Home },
                        onAnalyze = { url, uri ->
<<<<<<< HEAD
                            if (uri != null) viewModel.analyzeFile(context, uri)
                            else if (url != null) viewModel.analyze(url)
=======
                            if (uri != null) {
                                viewModel.analyzeFile(context, uri)
                            } else if (url != null) {
                                viewModel.analyze(url)
                            }
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                        },
                        isLoading = analysisState is AnalysisState.Loading
                    )
                    Screen.AnalyzeMessage -> AnalyzeMessageScreen(
                        onBack = { currentTab = Screen.Home },
<<<<<<< HEAD
                        onAnalyze = { text -> viewModel.analyze(text) },
=======
                        onAnalyze = { text ->
                            viewModel.analyze(text)
                        },
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                        isLoading = analysisState is AnalysisState.Loading
                    )
                    Screen.ScamAnalysis -> ScamAnalysisScreen(onBack = { currentTab = Screen.AnalyzeMessage })
                    Screen.EmergencyHub -> EmergencyHubScreen(
                        onBack = { currentTab = Screen.Home },
                        onRumorClick = { currentTab = Screen.FactCheckDetail }
                    )
                    Screen.FactCheckDetail -> FactCheckDetailScreen(onBack = { currentTab = Screen.EmergencyHub })
                    else -> HomeScreen(
                        isLoading = analysisState is AnalysisState.Loading,
<<<<<<< HEAD
                        onCheckCredibility = { input -> viewModel.analyze(input) },
=======
                        onCheckCredibility = { input ->
                            viewModel.analyze(input)
                        },
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                        onRecentItemClick = { _ ->
                            displayedResult = MockData.highCredibilityResult
                            showResults = true
                        }
                    )
                }
            }

<<<<<<< HEAD
            // Results detail screen
=======
            // Results detail screen — OVERLAID on top of tabs
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
            AnimatedVisibility(
                visible = showResults,
                enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            ) {
                displayedResult?.let { result ->
                    ResultsScreen(
                        result = result,
                        onBack = {
                            showResults = false
                            displayedResult = null
<<<<<<< HEAD
                            if (currentTab != Screen.Explore) viewModel.resetState()
=======
                            // Only reset state if NOT coming from Explore tab
                            if (currentTab != Screen.Explore) {
                                viewModel.resetState()
                            }
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                        },
                        onViewSource = { url ->
                            browserUrl = url
                            showResults = false
                            showBrowser = true
                        }
                    )
                }
            }

<<<<<<< HEAD
            // Browser screen
=======
            // Browser screen — OVERLAID on top
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
            AnimatedVisibility(
                visible = showBrowser,
                enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
                exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            ) {
                ExploreScreen(
                    viewModel = viewModel,
                    initialUrl = browserUrl,
                    isOverlay = true,
                    onOverlayBack = {
                        showBrowser = false
<<<<<<< HEAD
                        showResults = true
=======
                        showResults = true  // Go back to the analysis screen
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
                    }
                )
            }
        }
    }
}
