import os
from dotenv import load_dotenv

load_dotenv()

# We will use OpenAI API with the 'gpt-5-nano' model as requested by the user.
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = "gpt-5-nano"
