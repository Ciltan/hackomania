package com.example.myapplication.network

import com.example.myapplication.model.AnalysisResult
import com.example.myapplication.model.Claim
import com.example.myapplication.model.ClaimVerdict
import com.example.myapplication.model.CredibilityLevel
import com.example.myapplication.model.Source
import java.util.UUID

object ApiMapper {

    fun mapToInternal(apiResponse: ApiAnalysisResponse): AnalysisResult {
        return AnalysisResult(
            id = UUID.randomUUID().toString(),
            originalText = apiResponse.originalSubmission?.content ?: "",
            capturedTimeAgo = "Just now",
            credibilityLevel = when (apiResponse.credibilityLevel) {
                "RELIABLE" -> CredibilityLevel.HIGH
                "MISLEADING" -> CredibilityLevel.LOW
                else -> CredibilityLevel.UNVERIFIED
            },
            credibilityScore = (apiResponse.credibilityScore * 100).toInt(),
            credibilitySummary = apiResponse.credibilityLabel,
            claims = apiResponse.claimsBreakdown.map { apiClaim ->
                Claim(
                    text = apiClaim.title + ": " + apiClaim.description,
                    sources = apiClaim.sources.map { url ->
                        Source(title = "Source", url = url)
                    },
                    verdict = when (apiClaim.status) {
                        "VERIFIED" -> ClaimVerdict.VERIFIED
                        "FALSE" -> ClaimVerdict.FALSE
                        "CONTEXT_NEEDED" -> ClaimVerdict.MISLEADING
                        else -> ClaimVerdict.UNVERIFIABLE
                    }
                )
            },
            contextSummary = apiResponse.evidenceAnalysisSummary,
            sourceUrl = apiResponse.lateralReading.firstOrNull()?.url
        )
    }
}

fun ApiAnalysisResponse.toAnalysisResult(): AnalysisResult = ApiMapper.mapToInternal(this)
