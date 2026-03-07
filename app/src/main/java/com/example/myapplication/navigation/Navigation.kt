package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Results : Screen("results/{resultId}") {
        fun createRoute(resultId: String) = "results/$resultId"
    }
    object Browser : Screen("browser")
    object History : Screen("history")
    object Explore : Screen("explore")
    object Settings : Screen("settings")
    object UploadScreenshot : Screen("upload_screenshot")
    object VerifyVideo : Screen("verify_video")
    object AnalyzeMessage : Screen("analyze_message")
    object ScamAnalysis : Screen("scam_analysis")
    object EmergencyHub : Screen("emergency_hub")
    object FactCheckDetail : Screen("fact_check_detail")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val iconOutlined: String,
    val iconFilled: String
)
