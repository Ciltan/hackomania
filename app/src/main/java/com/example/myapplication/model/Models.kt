package com.example.myapplication.model

data class RecentAnalysis(
    val id: String,
    val title: String,
    val timeAgo: String,
    val credibilityScore: Int, // 0-100
    val credibilityLevel: CredibilityLevel
)

data class AnalysisResult(
    val id: String,
    val originalText: String,
    val capturedTimeAgo: String,
    val credibilityLevel: CredibilityLevel,
    val credibilityScore: Int,
    val credibilitySummary: String,
    val claims: List<Claim>,
    val contextSummary: String,
    val sourceUrl: String? = null
)

data class Claim(
    val text: String,
    val sources: List<Source>,
    val verdict: ClaimVerdict
)

data class Source(
    val title: String,
    val url: String
)

enum class CredibilityLevel {
    HIGH, MEDIUM, LOW, UNVERIFIED
}

enum class ClaimVerdict {
    VERIFIED, MISLEADING, FALSE, UNVERIFIABLE
}

data class ArticleContent(
    val url: String,
    val title: String,
    val body: String,
    val sourceReputation: SourceReputation,
    val warningMessage: String
)

enum class SourceReputation {
    TRUSTED, QUESTIONABLE, UNKNOWN
}
