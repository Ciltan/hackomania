from typing import List, Optional, Literal
from pydantic import BaseModel, Field

class SourceCredibility(BaseModel):
    name: str = Field(..., description="Name of the source (e.g., The Straits Times)")
    description: str = Field(..., description="Short text about the source's standards")
    has_trust_badge: bool = Field(False, description="Whether this is a highly trusted source")

class ClaimBreakdown(BaseModel):
    title: str = Field(..., description="Short title like 'Verified: Energy Subsidies'")
    description: str = Field(..., description="Explanation of the specific claim")
    status: Literal["VERIFIED", "CONTEXT_NEEDED", "FALSE"] = Field(..., description="The status of the claim")
    sources: List[str] = Field(..., description="Links to sources for this specific claim")

class LateralReadingSource(BaseModel):
    name: str = Field(..., description="Name of cross-referenced source (e.g., Reuters, BBC)")
    url: Optional[str] = Field(None, description="URL to the article if available")
    is_corroborating: bool = Field(..., description="True if it corroborates, False if it contradicts")
    summary: Optional[str] = Field(None, description="Short snippet of what they said")

class EvidenceItem(BaseModel):
    type: str = Field(..., description="Type of evidence (e.g., POSITIVE EVIDENCE FOUND)")
    content: str = Field(..., description="The evidence text")

class OriginalSubmission(BaseModel):
    content: str = Field(..., description="The text that was submitted")
    metadata: str = Field(..., description="Source and timestamp (e.g., Twitter • 2 hours ago)")

class AnalysisResponse(BaseModel):
    credibility_score: float = Field(..., description="Overall score from 0.0 to 1.0")
    credibility_label: str = Field(..., description="E.g., High Credibility")
    credibility_level: Literal["RELIABLE", "MISLEADING", "UNVERIFIED"] = Field(..., description="General credibility level")
    source_referenced_count: int = Field(0, description="Number of cross-referenced sources")
    
    original_submission: Optional[OriginalSubmission] = None
    source_credibility: Optional[SourceCredibility] = None
    evidence_analysis: List[EvidenceItem] = Field(default_factory=list, description="List of evidence findings")
    claims_breakdown: List[ClaimBreakdown] = Field(..., description="List of individual claim breakdowns")
    lateral_reading: List[LateralReadingSource] = Field(..., description="Cross-referenced sources")
    
    evidence_analysis_summary: str = Field(..., description="A couple of paragraphs of Context Summary")

class AnalyzeRequest(BaseModel):
    url: Optional[str] = None
    text: Optional[str] = None
