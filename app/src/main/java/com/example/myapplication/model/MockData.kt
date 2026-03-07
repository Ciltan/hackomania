package com.example.myapplication.model

object MockData {

    val recentAnalyses = listOf(
        RecentAnalysis(
            id = "1",
            title = "New MRT line expansion dates...",
            timeAgo = "2h ago",
            credibilityScore = 88,
            credibilityLevel = CredibilityLevel.HIGH
        ),
        RecentAnalysis(
            id = "2",
            title = "Government grants for seniors...",
            timeAgo = "5h ago",
            credibilityScore = 72,
            credibilityLevel = CredibilityLevel.MEDIUM
        ),
        RecentAnalysis(
            id = "3",
            title = "Weather alert: Flash floods in Central...",
            timeAgo = "1d ago",
            credibilityScore = 91,
            credibilityLevel = CredibilityLevel.HIGH
        )
    )

    val highCredibilityResult = AnalysisResult(
        id = "r1",
        originalText = "Government announces new energy subsidies starting July 2024 for middle-income households.",
        capturedTimeAgo = "2 mins ago",
        credibilityLevel = CredibilityLevel.HIGH,
        credibilityScore = 88,
        credibilitySummary = "Information aligns with trusted sources",
        claims = listOf(
            Claim(
                text = "\"Government announces new energy subsidies starting July 2024 for middle-income households.\"",
                sources = listOf(
                    Source("Straits Times: New Energy Grants", "https://straitstimes.com"),
                    Source("Ministry of Finance (MOF) Singapore", "https://mof.gov.sg")
                ),
                verdict = ClaimVerdict.VERIFIED
            ),
            Claim(
                text = "\"The subsidy covers 100% of all utility bill increases for the next five years.\"",
                sources = emptyList(),
                verdict = ClaimVerdict.MISLEADING
            )
        ),
        contextSummary = "This announcement aligns with recent Singapore Budget debates regarding the cost of living. However, the claim about \"100% coverage\" is exaggerated; official government releases specify tiered rebates based on HDB flat type rather than total bill coverage."
    )

    val lowCredibilityResult = AnalysisResult(
        id = "r2",
        originalText = "New Global Study Claims Coffee Extends Life Span by 40% Through Secret Enzyme",
        capturedTimeAgo = "Just now",
        credibilityLevel = CredibilityLevel.LOW,
        credibilityScore = 12,
        credibilitySummary = "Multiple red flags detected",
        claims = listOf(
            Claim(
                text = "\"Coffee extends life span by 40% through a secret enzyme found in certain bean varieties.\"",
                sources = emptyList(),
                verdict = ClaimVerdict.FALSE
            ),
            Claim(
                text = "\"Major pharmaceutical companies have suppressed this information for years.\"",
                sources = emptyList(),
                verdict = ClaimVerdict.UNVERIFIABLE
            )
        ),
        contextSummary = "This article contains multiple hallmarks of health misinformation: extraordinary claims without peer review, conspiracy allegations about suppression by corporations, and a researcher whose credentials are unverifiable. No credible medical institutions have confirmed these findings.",
        sourceUrl = "https://example-unreliable-news.com"
    )

    val articleContent = ArticleContent(
        url = "https://example-unreliable-news.com/coffee-study",
        title = "New Global Study Claims Coffee Extends Life Span by 40% Through Secret Enzyme",
        body = "A revolutionary new report published by an independent research group has sent shockwaves through the health community. The study suggests that a specific combination of caffeine and a previously undiscovered enzyme found in certain bean varieties can effectively reverse aging markers in human cells.\n\nAccording to the researchers, participants who drank more than six cups of coffee daily showed a miraculous 40% increase in total life expectancy compared to non-drinkers. This discovery contradicts decades of medical science, yet the authors insist the data is incontrovertible.\n\nThe group, which operates from an undisclosed offshore facility, claims that major pharmaceutical companies have been suppressing this information for years to protect their own longevity products.\n\n\"We believe this is the single greatest health breakthrough of the century,\" says lead investigator Dr. John Doe, whose credentials are currently under review by several medical boards.",
        sourceReputation = SourceReputation.QUESTIONABLE,
        warningMessage = "This source has a history of publishing unverified claims. Several phrases in this article match known misinformation patterns."
    )

    fun analyzeText(input: String): AnalysisResult {
        val lower = input.lowercase()
        val misinfoKeywords = listOf("secret", "100%", "cure", "banned", "suppressed", "miracle", "they don't want you to know")
        val hasMisinfo = misinfoKeywords.any { lower.contains(it) }

        return if (hasMisinfo) {
            lowCredibilityResult.copy(
                originalText = input,
                capturedTimeAgo = "Just now"
            )
        } else {
            highCredibilityResult.copy(
                originalText = input,
                capturedTimeAgo = "Just now"
            )
        }
    }
}
