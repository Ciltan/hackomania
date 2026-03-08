# Backend context for AI Agents

This directory contains documentation specifically designed to help AI coding assistants understand and integrate the backend with the frontend.

## Architecture Overview
- **Core**: FastAPI (Python 3.10+) using Pydantic for schemas and SQLAlchemy for persistence.
- **Key Services**:
    - `vision_service.py`: OCR for images/videos.
    - `scraper_service.py`: Article content fetching.
    - `llm_service.py`: Claim extraction and verdict generation (OpenAI).
    - `analyzer_orchestrator.py`: The main pipeline flow.

## Integration Points
The primary entry point for the frontend is `POST /api/v1/analyze`.
It accepts:
- `url`: (String) Target article.
- `text`: (String) Raw claim text.
- `file`: (Multipart) Image or Video for vision processing.

## Data Schema
The response follows the `AnalysisResponse` model in `backend/schemas/payloads.py`.
