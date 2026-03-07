from duckduckgo_search import DDGS

def search_web_for_evidence(query: str, max_results: int = 5):
    """Searches the web for corroborating or contradicting evidence regarding a claim."""
    results = []
    try:
        with DDGS() as ddgs:
            # 1. Try News Search first for high-credibility reporting
            search_results = list(ddgs.news(query, max_results=max_results))
            
            # 2. If no news results, fallback to general web search
            if not search_results:
                search_results = list(ddgs.text(query, max_results=max_results))
            
            for r in search_results:
                # News provides 'body', Text provides 'body' as well
                results.append({
                    "title": r.get("title", ""),
                    "url": r.get("href") or r.get("url", ""),
                    "source": r.get("source", "Web Source"),
                    "snippet": r.get("body", "")
                })
        return results
    except Exception as e:
        print(f"Error performing web search: {e}")
        return []
