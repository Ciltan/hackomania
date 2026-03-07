import requests
from bs4 import BeautifulSoup
import re

def scrape_url(url: str) -> str:
    """Fetches the content of a URL and extracts the main text."""
    try:
        # standard user agent to avoid basic blocks
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, "html.parser")

        # Aggressively remove noise elements
        for element in soup([
            "script", "style", "nav", "footer", "header", "aside", 
            "form", "iframe", "noscript", "button"
        ]):
            element.decompose()
            
        # Also remove elements often used for accessibility skip links or hidden text
        for element in soup.find_all(class_=re.compile(r'skip|hidden|visually-hidden|sr-only', re.I)):
            element.decompose()

        # Try to find the main article content
        main_content = None
        
        # Strategy 1: Look for <article> tag
        if soup.find("article"):
            main_content = soup.find("article")
        # Strategy 2: Look for <main> tag
        elif soup.find("main"):
            main_content = soup.find("main")
        # Strategy 3: Look for common content wrappers
        elif soup.find(class_=re.compile(r'article|post-content|main-content|story-body', re.I)):
            main_content = soup.find(class_=re.compile(r'article|post-content|main-content|story-body', re.I))
        # Fallback: Use the whole body
        else:
            main_content = soup.body if soup.body else soup

        # Get text from the selected container
        text = main_content.get_text(separator=' ')

        # Break into lines and remove leading and trailing space on each
        lines = (line.strip() for line in text.splitlines())
        # Break multi-headlines into a line each
        chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
        # Drop blank lines
        text = '\n'.join(chunk for chunk in chunks if chunk)
        
        # Limit text length to avoid token limits for basic scraping
        return text[:10000]

    except Exception as e:
        print(f"Error scraping {url}: {e}")
        return ""
