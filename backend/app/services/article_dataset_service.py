"""
Article Dataset Service (FACTors version)
Loads the FACTors fact-checked articles dataset (118k claims from 39 orgs)
and provides keyword-based search for broader verification.
"""
import csv
import os
from typing import List, Optional

# Path to the FACTors dataset
DATA_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data", "factors")
CSV_PATH = os.path.join(DATA_DIR, "FACTors.csv")

# FACTors scale mapping to internal levels
LABEL_MAP = {
    "true": "RELIABLE",
    "partially true": "MIXED",
    "false": "FALSE",
    "misleading": "MISLEADING",
    "unverifiable": "UNVERIFIED",
    "other": "UNVERIFIED",
}


class ArticleRecord:
    """Represents a single fact-checked claim from the FACTors dataset."""
    def __init__(self, row: dict):
        # Actual CSV headers: row_id,article_id,claim_id,claim,date_published,author,organisation,original_verdict,title,url,normalised_rating
        self.claim = row.get("claim", "")
        self.organisation = row.get("organisation", "Unknown Fact-Checker")
        self.verdict = row.get("normalised_rating", "unverifiable").lower()
        self.url = row.get("url", "")
        self.title = row.get("title", "")
        self.date = row.get("date_published", "")

    @property
    def credibility_level(self) -> str:
        return LABEL_MAP.get(self.verdict, "UNVERIFIED")

    def to_dict(self) -> dict:
        return {
            "name": f"{self.organisation} — {self.title[:60]}..." if self.title else self.organisation,
            "url": self.url,
            "label": self.verdict,
            "credibility_level": self.credibility_level,
            "statement": self.claim,
            "evidence_snippet": f"Fact-checked by {self.organisation} on {self.date}." if self.date else f"Fact-checked by {self.organisation}.",
            "context": self.title,
        }


class ArticleDatasetService:
    """Loads FACTors CSV and provides keyword-based search."""

    def __init__(self):
        self.articles: List[ArticleRecord] = []
        self._load_dataset()

    def _load_dataset(self):
        """Load the FACTors CSV file into memory."""
        if not os.path.exists(CSV_PATH):
            print(f"Warning: {CSV_PATH} not found. Run scripts/setup_factors_dataset.py first.")
            return

        try:
            with open(CSV_PATH, "r", encoding="utf-8", errors="ignore") as f:
                reader = csv.DictReader(f)
                count = 0
                for row in reader:
                    # Filter out empty or broken rows
                    # Headers are lowercase: claim, url, normalised_rating
                    if row.get("claim") and row.get("url"):
                        self.articles.append(ArticleRecord(row))
                        count += 1
            print(f"Loaded {len(self.articles)} fact-checked claims from FACTors dataset (Broader Coverage).")
        except Exception as e:
            print(f"Error loading FACTors dataset: {e}")

    def search_related(self, claim: str, max_results: int = 4) -> List[dict]:
        """
        Finds articles related to a claim using weighted keyword matching and title boosting.
        """
        if not claim or not self.articles:
            return []

        # Generic words that should have lower weight (0.5 instead of 1.0)
        generic_words = {
            "health", "news", "report", "people", "doctor", "hospital", "video", 
            "post", "media", "social", "claim", "verified", "latest", "update",
            "study", "science", "research", "evidence", "true", "false", "misleading"
        }

        # meaningfully extract keywords
        stop_words = {
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "to", "of", "in", "for",
            "on", "with", "at", "by", "from", "as", "into", "through", "during",
            "before", "after", "above", "below", "between", "and", "but", "or",
            "nor", "not", "so", "yet", "both", "either", "neither", "each",
            "every", "all", "any", "few", "more", "most", "other", "some", "such",
            "no", "only", "own", "same", "than", "too", "very", "just", "because",
            "if", "when", "where", "how", "what", "which", "who", "whom", "this",
            "that", "these", "those", "it", "its", "he", "she", "they", "we",
            "you", "i", "me", "my", "your", "his", "her", "our", "their", "said",
            "says", "about", "up", "out", "over", "also", "many", "much", "like",
            "even", "still", "well", "back", "then", "here", "there", "make",
            "made", "take", "took", "come", "came", "know", "think", "want",
            "need", "year", "years", "time", "going", "first", "last", "long", 
            "great", "little", "right", "good", "new", "used", "work", "state", 
            "states", "united", "country", "percent", "number", "million", 
            "billion", "according", "government", "national"
        }
        
        words = [w.lower().strip(".,!?\"'()[]{}:;") for w in claim.split()]
        weighted_keywords = {}
        for w in words:
            if len(w) > 3 and w not in stop_words:
                weight = 0.5 if w in generic_words else 1.0
                weighted_keywords[w] = weight

        if not weighted_keywords or len(weighted_keywords) < 2:
            return []

        scored = []
        total_possible_score = sum(weighted_keywords.values())
        
        for article in self.articles:
            score = 0.0
            found_count = 0
            
            # Lowercase for search
            title_text = article.title.lower() if article.title else ""
            claim_text = article.claim.lower()
            
            for kw, weight in weighted_keywords.items():
                # Title match is worth DOUBLE
                if kw in title_text:
                    score += weight * 2.0
                    found_count += 1
                elif kw in claim_text:
                    score += weight
                    found_count += 1
            
            # Normalization and thresholding
            # Must match at least 2 keywords AND have a decent total score
            if found_count >= 2:
                # We want a minimum "match quality" 
                # e.g. if we have 2 kws, we want at least a 1.2+ score (meaning at least one specific word or title hit)
                if score >= 1.2:
                    scored.append((score, found_count, article))

        if not scored:
            return []

        # Sort by relevance score primarily, then by count
        scored.sort(key=lambda x: (x[0], x[1]), reverse=True)
        
        # Relative thresholding: only keep top-tier matches
        best_score = scored[0][0]
        min_threshold = max(1.5, best_score * 0.6)
        
        filtered = [
            article.to_dict() 
            for score, cnt, article in scored 
            if score >= min_threshold
        ]

        return filtered[:max_results]


# Singleton instance
article_dataset = ArticleDatasetService()

