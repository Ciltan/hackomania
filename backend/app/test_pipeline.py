import asyncio
import json
from schemas.payloads import AnalyzeRequest
from services.analyzer_orchestrator import analyze_pipeline

def test_run():
    request = AnalyzeRequest(url="https://straitstimes.com/") # or some other test URL or text
    print("Running pipeline...")
    result = analyze_pipeline(request)
    print("Result:")
    print(result.model_dump_json(indent=2))

if __name__ == "__main__":
    test_run()
