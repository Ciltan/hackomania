# API Specification

## POST `/api/v1/analyze`
Analyzes a claim or article for credibility.

### Request (Multipart/Form-Data)
| Field | Type | Description |
| :--- | :--- | :--- |
| `url` | string | Optional URL |
| `text` | string | Optional raw text |
| `file` | file | Optional image/video |

### Response (`AnalysisResponse`)
```json
{
  "credibility_score": 0.85,
  "credibility_label": "High Credibility",
  "credibility_level": "RELIABLE",
  "source_referenced_count": 14,
  "original_submission": {
    "content": "...",
    "metadata": "..."
  },
  "source_credibility": {
    "name": "...",
    "type": "...",
    "has_trust_badge": true
  },
  "evidence_analysis": [...],
  "claims_breakdown": [...],
  "lateral_reading": [...],
  "evidence_analysis_summary": "..."
}
```
