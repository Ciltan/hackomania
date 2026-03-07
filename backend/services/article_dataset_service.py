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
        Finds articles related to a claim using keyword matching.
        """
        if not claim or not self.articles:
            return []

        # Extract meaningful keywords
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
            "need", "people", "year", "years", "time", "says", "going", "been",
            "first", "last", "long", "great", "little", "right", "good", "new",
            "used", "work", "state", "states", "united", "country", "percent",
            "number", "million", "billion", "according", "government", "national"
        }
        keywords = [
            w.lower().strip(".,!?\"'()[]{}:;")
            for w in claim.split()
            if len(w) > 3 and w.lower().strip(".,!?\"'()[]{}:;") not in stop_words
        ]

        if not keywords or len(keywords) < 2:
            return []

        num_keywords = len(keywords)
        scored = []
        
        # Search across 118k entries (approx 100-200ms)
        for article in self.articles:
            # Match against claim text and title
            search_text = (article.claim + " " + article.title).lower()
            matched = sum(1 for kw in keywords if kw in search_text)
            
            match_pct = matched / num_keywords if num_keywords > 0 else 0
            
            # Lowering threshold slightly: 30% match OR at least 2 strong keywords
            if match_pct >= 0.30 and matched >= 2:
                scored.append((match_pct, matched, article))

        if not scored:
            return []

        # Sort by relevance
        scored.sort(key=lambda x: (x[0], x[1]), reverse=True)
        
        # Thresholding to keep only top matches
        top_pct = scored[0][0]
        min_threshold = max(0.40, top_pct * 0.7)
        
        filtered = [
            article.to_dict() 
            for pct, cnt, article in scored 
            if pct >= min_threshold
        ]

        return filtered[:max_results]


# Singleton instance
article_dataset = ArticleDatasetService()
