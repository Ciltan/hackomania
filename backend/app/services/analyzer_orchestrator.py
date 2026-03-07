from schemas.payloads import AnalyzeRequest, AnalysisResponse, SourceCredibility
from services.scraper_service import scrape_url
from services.dataset_service import dataset_check
from services.article_dataset_service import article_dataset
from services.llm_service import extract_primary_claim, formulate_verdict
from services.search_service import search_web_for_evidence
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
    
    # 4. Find related fact-checked articles (Local Dataset + Web Fallback)
    related_articles = article_dataset.search_related(llm_claim.claim, max_results=4)
    
    # If we have very few high-quality local results, supplement with web search
    if len(related_articles) < 2:
        web_results = search_web_for_evidence(llm_claim.claim, max_results=3)
        for res in web_results:
            # Transform web result to match article format
            related_articles.append({
                "name": f"Web Evidence: {res['source']}",
                "url": res['url'],
                "label": "unverified",  # Web results aren't formal verdicts
                "credibility_level": "UNVERIFIED",
                "statement": res['title'],
                "evidence_snippet": res['snippet'],
                "context": f"Search Result from {res['source']}"
            })

    print(f"\n[EVIDENCE SOURCE] Gathering evidence for claim: '{llm_claim.claim}'", flush=True)
    for i, a in enumerate(related_articles):
        source_type = "Web Search" if "Web Evidence:" in a['name'] else "FACTors Dataset"
        print(f"  --> {i+1}: [{source_type}] {a['name']}", flush=True)
    # 5. Final Verdict and Formatting
    try:
        final_verdict = formulate_verdict(
            original_text=content_to_analyze,
            domain_context=domain_context,
            claim=llm_claim.claim,
            domain_credibility=domain_credibility,
            related_articles=related_articles[:6]  # Cap results for LLM context
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
        evidence_analysis_summary=message or "The system could not find enough information to verify this claim."
    )
