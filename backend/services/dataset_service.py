import csv
import os
from typing import Dict, Optional

# Path to the downloaded CRED-1 dataset
DATASET_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "cred-1", "data", "cred1_current.csv")

class CredibilityDataset:
    def __init__(self):
        self.domains: Dict[str, dict] = {}
        self._load_dataset()

    def _load_dataset(self):
        """Loads the CRED-1 dataset into memory on initialization."""
        if not os.path.exists(DATASET_PATH):
            print(f"Warning: Credibility dataset not found at {DATASET_PATH}")
            return
            
        with open(DATASET_PATH, mode="r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                domain = row.get("domain", "").strip().lower()
                if domain:
                    self.domains[domain] = {
                        "category": row.get("category", "unknown"),
                        "credibility_score": float(row.get("credibility_score", 0.0) or 0.0),
                        "iffy_factual": row.get("iffy_factual", ""),
                        "iffy_bias": row.get("iffy_bias", "")
                    }
        print(f"Loaded {len(self.domains)} domains from CRED-1 dataset.")

    def check_domain(self, target_domain: str) -> Optional[dict]:
        """Looks up a domain or its root in the dataset."""
        target_domain = target_domain.lower().strip()
        
        # Check exact match
        if target_domain in self.domains:
            return self.domains[target_domain]
            
        # Check root domain (e.g., if target is www.breitbart.com, check breitbart.com)
        parts = target_domain.split('.')
        if len(parts) > 2:
            root_domain = '.'.join(parts[-2:])
            if root_domain in self.domains:
                return self.domains[root_domain]
                
        return None

# Singleton instance
dataset_check = CredibilityDataset()
