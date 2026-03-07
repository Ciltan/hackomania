"""Quick test of the article dataset search."""
from services.article_dataset_service import article_dataset

# Test search
results = article_dataset.search_related(
    "The economy is growing and unemployment is at a record low",
    max_results=3
)

print(f"Found {len(results)} related articles:")
for r in results:
    print(f"  URL: {r['url']}")
    print(f"  Label: {r['label']} ({r['credibility_level']})")
    print(f"  Statement: {r['statement'][:80]}...")
    print()
