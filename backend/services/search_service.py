from duckduckgo_search import DDGS

def search_web_for_evidence(query: str, max_results: int = 5):
    """Searches the web for corroborating or contradicting evidence regarding a claim."""
    results = []
    try:
        with DDGS() as ddgs:
            # We search news for credibility
            search_results = ddgs.news(query, max_results=max_results)
            for r in search_results:
                results.append({
                    "title": r.get("title", ""),
                    "url": r.get("url", ""),
                    "source": r.get("source", ""),
                    "snippet": r.get("body", "")
                })
        return results
    except Exception as e:
        print(f"Error performing web search: {e}")
        return []
