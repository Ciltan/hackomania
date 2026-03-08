package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

// ── Models matching the new Modular Backend (schemas/payloads.py) ──

data class ApiSourceCredibility(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("has_trust_badge") val hasTrustBadge: Boolean
)

data class ApiClaimSource(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String?
)

data class ApiClaimBreakdown(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String, // "VERIFIED", "CONTEXT_NEEDED", "FALSE"
    @SerializedName("sources") val sources: List<ApiClaimSource>
)

data class ApiLateralReadingSource(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String?,
    @SerializedName("is_corroborating") val isCorroborating: Boolean,
    @SerializedName("summary") val summary: String?
)

data class ApiEvidenceItem(
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String
)

data class ApiOriginalSubmission(
    @SerializedName("content") val content: String,
    @SerializedName("metadata") val metadata: String
)

data class ApiAnalysisResponse(
    @SerializedName("credibility_score") val credibilityScore: Float,
    @SerializedName("credibility_label") val credibilityLabel: String,
    @SerializedName("credibility_level") val credibilityLevel: String, // "RELIABLE", "MISLEADING", "UNVERIFIED"
    @SerializedName("source_referenced_count") val sourceReferencedCount: Int,
    @SerializedName("original_submission") val originalSubmission: ApiOriginalSubmission?,
    @SerializedName("source_credibility") val sourceCredibility: ApiSourceCredibility?,
    @SerializedName("evidence_analysis") val evidenceAnalysis: List<ApiEvidenceItem> = emptyList(),
    @SerializedName("claims_breakdown") val claimsBreakdown: List<ApiClaimBreakdown> = emptyList(),
    @SerializedName("lateral_reading") val lateralReading: List<ApiLateralReadingSource> = emptyList(),
    @SerializedName("evidence_analysis_summary") val evidenceAnalysisSummary: String
)

data class ApiTranslateResponse(
    @SerializedName("translated_text") val translatedText: String
)

data class ApiChatResponse(
    @SerializedName("answer") val answer: String
)
