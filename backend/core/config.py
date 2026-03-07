import os
from dotenv import load_dotenv
from openai import OpenAI

# Explicitly load .env from the current directory
load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = "gpt-4o"

_client = None

def get_openai_client():
    global _client
    if _client is None:
        key = os.getenv("OPENAI_API_KEY")
        if not key:
            # Try to load again just in case
            load_dotenv()
            key = os.getenv("OPENAI_API_KEY")
            
        if not key:
            print("WARNING: OPENAI_API_KEY not found in environment!")
            
        _client = OpenAI(
            api_key=key,
            timeout=60.0,  # 60 second timeout for all requests
        )
    return _client
