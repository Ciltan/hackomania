import os
import requests

# Path relative to this script in /scripts folder
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_DIR = os.path.join(BASE_DIR, "data", "factors")
CSV_URL = "https://raw.githubusercontent.com/altuncu/FACTors/main/data/FACTors.csv"
TARGET_PATH = os.path.join(DATA_DIR, "FACTors.csv")

def setup():
    if not os.path.exists(DATA_DIR):
        os.makedirs(DATA_DIR, exist_ok=True)
        print(f"Created directory: {DATA_DIR}")

    if os.path.exists(TARGET_PATH) and os.path.getsize(TARGET_PATH) > 1000000:
        print(f"Dataset already exists at {TARGET_PATH}")
        return

    print(f"Downloading FACTors dataset from {CSV_URL}...")
    print("This may take a minute (approx 50MB)...")
    
    try:
        response = requests.get(CSV_URL, stream=True, timeout=30)
        response.raise_for_status()
        
        with open(TARGET_PATH, "wb") as f:
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    f.write(chunk)
                    
        print(f"Successfully downloaded FACTors dataset (~118k claims) to {TARGET_PATH}")
    except Exception as e:
        print(f"Error downloading dataset: {e}")
        print("Please manually download it from the URL and place it in the data/factors/ folder.")

if __name__ == "__main__":
    setup()
