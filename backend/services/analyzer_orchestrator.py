from schemas.payloads import AnalyzeRequest, AnalysisResponse, SourceCredibility
from services.scraper_service import scrape_url
from services.dataset_service import dataset_check
from services.article_dataset_service import article_dataset
from services.llm_service import extract_primary_claim, formulate_verdict
import urllib.parse

def analyze_pipeline(request: AnalyzeRequest, domain_override: str = None) -> AnalysisResponse:
    """The central orchestration of scraping, analyzing, searching, and grading."""
    # 1. Input Processing
    domain_context = "User provided text without a specific domain."
    content_to_analyze = ""
    
    if request.url:
        parsed_uri = urllib.parse.urlparse(request.url)
        domain = '{uri.netloc}'.format(uri=parsed_uri)
        domain_context = f"The claim originates from the domain: {domain}."
        
        scraped_text = scrape_url(request.url)
        content_to_analyze = scraped_text
    elif request.text:
        content_to_analyze = request.text
        
    if not content_to_analyze:
        # Fallback empty response
        return _fallback_error_response("Failed to retrieve content or content was empty.")

    # 2. Claim Extraction
    llm_claim = extract_primary_claim(content_to_analyze)

    # 3. Credibility Dataset Lookup
    domain_credibility = None
    if request.url:
        parsed_uri = urllib.parse.urlparse(request.url)
        target_domain = '{uri.netloc}'.format(uri=parsed_uri)
        domain_credibility = dataset_check.check_domain(target_domain)
    elif domain_override:
        # Use the domain detected from an uploaded image/video
        domain_context = f"The claim originates from the domain (detected from image/video): {domain_override}."
        domain_credibility = dataset_check.check_domain(domain_override)
    
    # 4. Find related fact-checked articles from LIAR-PLUS dataset
    related_articles = article_dataset.search_related(llm_claim.claim, max_results=4)

    # 5. Final Verdict and Formatting
    try:
        final_verdict = formulate_verdict(
            original_text=content_to_analyze,
            domain_context=domain_context,
            claim=llm_claim.claim,
            domain_credibility=domain_credibility,
            related_articles=related_articles
        )
        return final_verdict
    except Exception as e:
        return _fallback_error_response(f"LLM Error during analysis: {str(e)}")


def _fallback_error_response(message: str) -> AnalysisResponse:
    """Returns a safe structured response if something critical breaks."""
    return AnalysisResponse(
        credibility_score=0,
        credibility_label="Error during Analysis",
        credibility_level="UNVERIFIED",
        source_credibility=SourceCredibility(
            name="System",
            description="The analysis pipeline encountered an error.",
            has_trust_badge=False
        ),
        claims_breakdown=[],
        lateral_reading=[],
        evidence_analysis_summary=message
    )
