import json
from typing import List, Optional
from core.config import get_openai_client, OPENAI_MODEL
from schemas.payloads import AnalysisResponse, ClaimBreakdown, LateralReadingSource, SourceCredibility, EvidenceItem, OriginalSubmission

class AnalyzeRequest_Internal:
    def __init__(self, claim: str):
        self.claim = claim

def extract_primary_claim(text: str) -> AnalyzeRequest_Internal:
    """
    Uses LLM to distill a concise, searchable claim from potentially messy OCR text.
    Handles 'DOMAIN:', 'HEADLINE:', and 'CONTENT:' tags from vision_service.
    """
    prompt = f"""
    You are an AI assistant that extracts the single most important fact-checkable claim from a block of text.
    The text may contain OCR metadata like 'DOMAIN:', 'HEADLINE:', and 'CONTENT:'.
    
    TEXT:
    {text}
    
    Respond ONLY with a single concise sentence (max 20 words) representing the main claim.
    If multiple claims exist, pick the most sensational or important one.
    """
    try:
        client = get_openai_client()
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": "Extract the primary claim. Be concise."},
                {"role": "user", "content": prompt}
            ],
            temperature=0,
            max_tokens=60
        )
        claim_text = response.choices[0].message.content.strip()
        return AnalyzeRequest_Internal(claim_text)
    except Exception as e:
        print(f"Error in extract_primary_claim: {e}")
        # Fallback to simple slicing if LLM fails
        return AnalyzeRequest_Internal(text[:200] + "..." if len(text) > 200 else text)

def formulate_verdict(
    original_text: str,
    domain_context: str,
    claim: str,
    domain_credibility: Optional[dict] = None,
    related_articles: List[dict] = []
) -> AnalysisResponse:
    """
    Calls OpenAI to perform a detailed fact-check using lateral reading.
    Matches the schema in schemas/payloads.py
    """
    prompt = f"""
    You are an expert fact-checker specializing in Singapore-related news and global misinformation.
    Analyze the following content and provide a structured JSON response.

    CONTENT TO ANALYZE (MAY BE RAW OCR):
    {original_text}

    CONTEXT:
    {domain_context}
    DISTILLED CLAIM: {claim}

    ADDITIONAL DATA:
    - Domain Credibility: {json.dumps(domain_credibility) if domain_credibility else "Unknown"}
    - Similar Fact-Checks found in database: {json.dumps(related_articles)}

    GUIDELINES:
    1. Evaluate the content using 'Lateral Reading': verify it by looking at other sources.
    2. Since the content might be OCR from a screenshot or video, ignore typos but focus on the core meaning.
    3. For Singapore context, prioritize cross-referencing with news outlets like The Straits Times, CNA, and official Gov.sg advisories.
    4. Provide specific sources in the 'lateral_reading' section.

    Your output MUST be a valid JSON matching this schema:
    {{
      "credibility_score": float (0.0 to 1.0),
      "credibility_label": string (e.g. "Verified", "Misleading", "False"),
      "credibility_level": "RELIABLE" | "MISLEADING" | "UNVERIFIED",
      "source_referenced_count": int,
      "original_submission": {{
        "content": string (the distilled claim or headline),
        "metadata": "{domain_context}"
      }},
      "source_credibility": {{
        "name": string,
        "description": string,
        "has_trust_badge": boolean
      }},
      "evidence_analysis": [
        {{ "type": "POSITIVE EVIDENCE FOUND" | "CONTRADICTION FOUND", "content": string }}
      ],
      "claims_breakdown": [
        {{
          "title": string,
          "description": string,
          "status": "VERIFIED" | "CONTEXT_NEEDED" | "FALSE",
          "sources": ["url1", "url2"]
        }}
      ],
      "lateral_reading": [
        {{
          "name": string,
          "url": string,
          "is_corroborating": boolean,
          "summary": string
        }}
      ],
      "evidence_analysis_summary": "A detailed 2-paragraph summary explaining the verdict and the evidence found."
    }}
    """

    try:
        client = get_openai_client()
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": "You are a professional fact-checker. Output only valid JSON."},
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"}
        )

        result_json = json.loads(response.choices[0].message.content)
        
        # Ensure the response matches the AnalysisResponse model
        return AnalysisResponse(**result_json)

    except Exception as e:
        print(f"Error in formulate_verdict: {e}")
        # Return a fallback response
        return AnalysisResponse(
            credibility_score=0.5,
            credibility_label="Analysis Error",
            credibility_level="UNVERIFIED",
            source_referenced_count=0,
            original_submission=OriginalSubmission(content=claim, metadata="Unknown"),
            source_credibility=SourceCredibility(name="Error", description=f"Analysis pipeline crashed: {str(e)}", has_trust_badge=False),
            evidence_analysis=[],
            claims_breakdown=[],
            lateral_reading=[],
            evidence_analysis_summary=f"Analysis failed due to an error: {str(e)}"
        )
