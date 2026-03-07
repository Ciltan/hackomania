import sys
import os

# Add the app directory to sys.path
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(BASE_DIR)

from services.article_dataset_service import article_dataset

def test_search():
    print("Testing FACTors Dataset Search...")
    
    test_claims = [
        "drinking bleach cures covid",  # Health/Rumor (Snopes/Health)
        "biden wins 2024 election",     # Politics (Reuters/AP)
        "earth is flat and stationary", # Science/Conspiracy
        "singapore inflation rate 2025" # Fact-heavy news
    ]
    
    for claim in test_claims:
        print(f"\nSearching for: '{claim}'")
        results = article_dataset.search_related(claim, max_results=3)
        
        if not results:
            print("  No matches found.")
            continue
            
        for i, res in enumerate(results):
            print(f"  [{i+1}] {res['name']}")
            print(f"      Verdict: {res['label']} ({res['credibility_level']})")
            print(f"      Claim in Data: {res['statement'][:100]}...")
            print(f"      URL: {res['url']}")

if __name__ == "__main__":
    if not os.path.exists(os.path.join(BASE_DIR, "data", "factors", "FACTors.csv")):
        print("ERROR: FACTors.csv NOT FOUND. Run setup_factors_dataset.py first.")
    else:
        test_search()
