import json
from pydantic import BaseModel
from openai import OpenAI
from core.config import OPENAI_API_KEY, OPENAI_MODEL
from schemas.payloads import AnalysisResponse

client = OpenAI(api_key=OPENAI_API_KEY)

def scrub_schema(schema: dict) -> dict:
    """Removes keys from a json schema that OpenAI strict function calling rejects."""
    if not isinstance(schema, dict):
        return schema
    keys_to_remove = ["title", "default_factory"]
    scraped = {}
    for k, v in schema.items():
        if k in keys_to_remove:
            continue
        if isinstance(v, dict):
            scraped[k] = scrub_schema(v)
        elif isinstance(v, list):
            scraped[k] = [scrub_schema(i) if isinstance(i, dict) else i for i in v]
        else:
            scraped[k] = v
    return scraped

class ExtractedClaim(BaseModel):
    claim: str
    context: str

def extract_primary_claim(text: str) -> ExtractedClaim:
    """Uses LLM to quickly extract the core factual claim being made."""
    try:
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": "You are a specialized fact-checking assistant. Extract the single primary factual claim from the text and a brief context."},
                {"role": "user", "content": text[:8000]} # Limit text length sent
            ],
            tools=[{
                "type": "function",
                "function": {
                    "name": "extract_claim",
                    "description": "Extracts the primary claim.",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "claim": {"type": "string"},
                            "context": {"type": "string"}
                        },
                        "required": ["claim", "context"],
                        "additionalProperties": False
                    }
                }
            }],
            tool_choice={"type": "function", "function": {"name": "extract_claim"}}
        )
        tool_call = response.choices[0].message.tool_calls[0]
        args = json.loads(tool_call.function.arguments)
        return ExtractedClaim(**args)
    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"Error extracting claim: {e}")
        return ExtractedClaim(claim="Unable to extract claim.", context="Error parsing text with LLM.")

def extract_keywords(claim: str) -> str:
    """Uses LLM to extract 3-5 core search keywords from a verbose claim to optimize web search."""
    try:
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": "Extract 3 to 5 critical search keywords from the given claim. Return ONLY the keywords separated by spaces. Do not use quotes or punctuation."},
                {"role": "user", "content": claim}
            ]
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        print(f"Error extracting keywords: {e}")
        # On error, just return the first 5 words of the claim as a fallback
        return " ".join(claim.split()[:5])


def formulate_verdict(original_text: str, domain_context: str, claim: str, domain_credibility: dict, related_articles: list = None) -> AnalysisResponse:
    """Uses LLM and structural outputs to generate the final analytical json matching the schema."""
    try:
        credibility_str = json.dumps(domain_credibility, indent=2) if domain_credibility else "No open-source dataset rating found for this domain."
        
        # Build the related articles context for lateral reading
        articles_context = ""
        if related_articles:
            articles_context = "\n\nProvided Evidence (use these EXACT sources for lateral reading):\n"
            for i, article in enumerate(related_articles, 1):
                articles_context += f"""
Evidence {i}:
- Source: {article.get('name', 'Web Source')}
- URL: {article.get('url', '')}
- Label: {article.get('label', 'unknown')} ({article.get('credibility_level', 'UNVERIFIED')})
- Statement: {article.get('statement', '')}
- Evidence: {article.get('evidence_snippet', '')}
"""
        
        system_prompt = f"""
As a specialized fact-checking AI, you evaluate claims by synthesizing provided domain reputation and lateral reading evidence.

### Scoring & Labels
- `credibility_score`: (0.0 to 1.0) Final truthfulness score.
  - **Overruling Policy**: Fact-checker findings OVERRULE domain reputation. If multiple reliable fact-checkers label the claim as FALSE or MISLEADING, the score MUST be below 0.3 even if the source domain has a high score.
  - **No Info Policy**: If no domain metrics are provided, evaluate based ONLY on fact-checks. If no fact-checks are found either, return a score of 0.5 (Neutral/Unverified).
- `credibility_label`: A descriptive phrase (e.g., "Debunked by Snopes" or "Reliable Medical Source").
- `credibility_level`: EXACTLY ONE of: RELIABLE, MIXED, MISLEADING, FALSE, UNVERIFIED.

### Lateral Reading Logic
- `lateral_reading`: You MUST ONLY use the EXACT sources provided in the "Provided Evidence" section below. 
  - DO NOT invent, hallucinate, or guess URLs.
  - DO NOT create fake fact-checks. 
  - If the "Provided Evidence" section is empty or irrelevant to the claim, leave the `lateral_reading` array EMPTY.
- `is_corroborating`: Set to `true` if the evidence AGREES with the user's content.
  - **Example**: If the user says "Earth is flat" and the article says "Earth is flat is FALSE", set `is_corroborating: false`.
  - **Example**: If the user says "Earth is flat" and the article says "Earth is flat is TRUE", set `is_corroborating: true`.
  - **Reverse Example**: If a fact-checker debunks a CONTRARY claim (e.g., "Vaccines are poison is FALSE"), that supports the user's claim that vaccines are safe -> `is_corroborating: true`.

---
**INPUT CONTEXT**
- Original Content: {original_text[:2000]}
- Domain Context: {domain_context}
- Domain Dataset Metrics: {credibility_str}
- {articles_context}
"""
        user_prompt = f"Primary Claim: {claim}\nPlease evaluate this leveraging the dataset metrics provided."
        
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            tools=[{
                "type": "function",
                "function": {
                    "name": "formulate_verdict",
                    "description": "Formulates the final analysis and verdict.",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "credibility_score": {"type": "number"},
                            "credibility_label": {"type": "string"},
                            "credibility_level": {"type": "string", "enum": ["RELIABLE", "MIXED", "MISLEADING", "FALSE", "UNVERIFIED"]},
                            "source_credibility": {
                                "type": ["object", "null"],
                                "properties": {
                                    "name": {"type": "string"},
                                    "description": {"type": "string"},
                                    "has_trust_badge": {"type": "boolean"}
                                },
                                "required": ["name", "description", "has_trust_badge"],
                                "additionalProperties": False
                            },
                            "claims_breakdown": {
                                "type": "array",
                                "items": {
                                    "type": "object",
                                    "properties": {
                                        "title": {"type": "string"},
                                        "description": {"type": "string"},
                                        "status": {"type": "string", "enum": ["VERIFIED", "CONTEXT_NEEDED", "FALSE"]},
                                        "sources": {
                                            "type": "array",
                                            "items": {
                                                "type": "object",
                                                "properties": {
                                                    "name": {"type": "string"},
                                                    "url": {"type": ["string", "null"]}
                                                },
                                                "required": ["name", "url"],
                                                "additionalProperties": False
                                            }
                                        }
                                    },
                                    "required": ["title", "description", "status", "sources"],
                                    "additionalProperties": False
                                }
                            },
                            "lateral_reading": {
                                "type": "array",
                                "items": {
                                    "type": "object",
                                    "properties": {
                                        "name": {"type": "string"},
                                        "url": {"type": ["string", "null"]},
                                        "is_corroborating": {"type": "boolean"},
                                        "summary": {"type": ["string", "null"]}
                                    },
                                    "required": ["name", "url", "is_corroborating", "summary"],
                                    "additionalProperties": False
                                }
                            },
                            "evidence_analysis_summary": {"type": "string"}
                        },
                        "required": [
                            "credibility_score", 
                            "credibility_label", 
                            "credibility_level", 
                            "source_credibility",
                            "claims_breakdown", 
                            "lateral_reading", 
                            "evidence_analysis_summary"
                        ],
                        "additionalProperties": False
                    }
                }
            }],
            tool_choice={"type": "function", "function": {"name": "formulate_verdict"}}
        )
        tool_call = response.choices[0].message.tool_calls[0]
        args = json.loads(tool_call.function.arguments)
        return AnalysisResponse(**args)

    except Exception as e:
        import traceback
        traceback.print_exc()
        print(f"Error formulating verdict: {e}")
        raise e

def filter_evidence_relevance(claim: str, articles: list) -> list:
    """Uses LLM to filter a list of candidate articles, discarding completely irrelevant ones."""
    if not articles:
        return []
        
    try:
        articles_context = ""
        for i, article in enumerate(articles):
            articles_context += f"\n[{i}] Title: {article.get('context', '')}\nClaim Checked: {article.get('statement', '')}\n"
            
        system_prompt = f"""
        You are a relevance filtering assistant. Your task is to look at a user's `Primary Claim` and a list of `Candidate Articles`.
        Return ONLY the list of indices `[0, 1, ...]` of the candidate articles that are genuinely relevant to verifying or debunking the primary claim.
        If an article just shares a keyword but is talking about a completely different event or claim, EXCLUDE its index.
        If none are relevant, return an empty list `[]`.
        
        Candidate Articles:
        {articles_context}
        """
        
        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": f"Primary Claim: {claim}"}
            ],
            tools=[{
                "type": "function",
                "function": {
                    "name": "filter_articles",
                    "description": "Selects the indices of relevant articles.",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "relevant_indices": {
                                "type": "array",
                                "items": {"type": "integer"}
                            }
                        },
                        "required": ["relevant_indices"],
                        "additionalProperties": False
                    }
                }
            }],
            tool_choice={"type": "function", "function": {"name": "filter_articles"}}
        )
        
        tool_call = response.choices[0].message.tool_calls[0]
        args = json.loads(tool_call.function.arguments)
        relevant_indices = args.get("relevant_indices", [])
        
        # Guard against LLM hallucinating out-of-bounds indices
        filtered_articles = [articles[i] for i in relevant_indices if 0 <= i < len(articles)]
        
        print(f"[LLM FILTER] Original count: {len(articles)} -> Filtered count: {len(filtered_articles)}")
        return filtered_articles
        
    except Exception as e:
        print(f"Error during evidence filtering: {e}")
        # On error, safely return the original unfiltered list to not break the pipeline
        return articles

