from schemas.payloads import AnalyzeRequest, AnalysisResponse, SourceCredibility, OriginalSubmission
from services.scraper_service import scrape_url
from services.dataset_service import dataset_check
from services.article_dataset_service import article_dataset
from services.llm_service import extract_primary_claim, formulate_verdict, filter_evidence_relevance
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
    
    # 4. Find related fact-checked articles (Local Dataset + LLM Filter + Web Fallback)
    related_articles = article_dataset.search_related(llm_claim.claim, max_results=5)
    
    # Use LLM to vigorously filter out irrelevant matches gathered from the local dataset step
    if related_articles:
        related_articles = filter_evidence_relevance(llm_claim.claim, related_articles)
    
    # If we have very few high-quality local results left after filtering, supplement with web search
    if len(related_articles) < 2:
        from services.llm_service import extract_keywords
        search_query = extract_keywords(llm_claim.claim)
        print(f"  [DuckDuckGo] Searching for keywords: '{search_query}'")
        
        web_results = search_web_for_evidence(search_query, max_results=3)
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

    print("\n" + "="*50)
    print("FINAL COMPILED EVIDENCE SOURCES:")
    for i, a in enumerate(related_articles):
        source_type = "Web Search" if "Web Evidence:" in a['name'] else "FACTors Dataset"
        title = a.get("statement") or a.get("title") or a.get("name") or "Unknown Title"
        print(f"[{i+1}] [{source_type}] {title}")
    print("="*50 + "\n", flush=True)
    # 5. Final Verdict and Formatting
    try:
        final_verdict = formulate_verdict(
            original_text=content_to_analyze,
            domain_context=domain_context,
            claim=llm_claim.claim,
            domain_credibility=domain_credibility,
            related_articles=related_articles[:6]  # Cap results for LLM context
        )
        # Populate the original submission so the app can display it
        final_verdict.original_submission = OriginalSubmission(
            content=content_to_analyze[:2000],
            metadata=f"{request.url or 'Direct Text'} • Just now"
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
