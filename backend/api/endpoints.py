import hashlib
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from schemas.payloads import AnalyzeRequest, AnalysisResponse
from services.analyzer_orchestrator import analyze_pipeline
from services.vision_service import extract_text_from_image, extract_text_from_video
from core.database import get_db
from models.analysis import AnalysisRecord

router = APIRouter()

def get_input_hash(content: str) -> str:
    return hashlib.sha256(content.encode('utf-8')).hexdigest()

@router.post("/analyze", response_model=AnalysisResponse)
async def analyze_content(
    url: Optional[str] = Form(None),
    text: Optional[str] = Form(None),
    file: Optional[UploadFile] = File(None),
    db: Session = Depends(get_db)
):
    """
    Analyze content for factuality. Accepts three input modes:
    - **url**: A link to an article to scrape and analyze.
    - **text**: Raw text of a claim to analyze directly.
    - **file**: An image (screenshot of article) or video to extract text from via AI vision.
    """
    extracted_text = None
    detected_domain = None

    # --- Mode 1: File upload (image or video) ---
    if file:
        file_bytes = await file.read()
        content_type = file.content_type or ""

        if content_type.startswith("image/"):
            extracted_text = extract_text_from_image(file_bytes, mime_type=content_type)
        elif content_type.startswith("video/"):
            ext = "." + (content_type.split("/")[-1] if "/" in content_type else "mp4")
            extracted_text = extract_text_from_video(file_bytes, file_extension=ext)
        else:
            raise HTTPException(status_code=400, detail=f"Unsupported file type: {content_type}. Upload an image or video.")

        if not extracted_text or extracted_text.startswith("Error:"):
            raise HTTPException(status_code=422, detail=f"Could not extract text from uploaded file: {extracted_text}")

        # Try to parse domain from vision output
        for line in extracted_text.split("\n"):
            if line.upper().startswith("DOMAIN:"):
                parsed_domain = line.split(":", 1)[1].strip().lower()
                if parsed_domain and parsed_domain != "unknown":
                    detected_domain = parsed_domain
                break

    # --- Determine the final content to analyze ---
    content_to_analyze = extracted_text or text
    final_url = url

    if not final_url and not content_to_analyze:
        raise HTTPException(status_code=400, detail="Must provide either a url, text, or an image/video file to analyze.")

    # Build request object for the orchestrator
    request = AnalyzeRequest(url=final_url, text=content_to_analyze)

    # Use extracted domain override if we detected one from the image
    cache_key = final_url or content_to_analyze
    input_hash = get_input_hash(cache_key)
    
    # Check cache
    cached_record = db.query(AnalysisRecord).filter(AnalysisRecord.input_hash == input_hash).first()
    if cached_record:
        print("Returning cached analysis.")
        return AnalysisResponse(**cached_record.response_json)

    # Run the full pipeline
    try:
        response = analyze_pipeline(request, domain_override=detected_domain)
        
        # Save to database cache
        new_record = AnalysisRecord(
            input_hash=input_hash,
            url=final_url,
            text_content=content_to_analyze,
            response_json=response.model_dump()
        )
        db.add(new_record)
        db.commit()
        
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Pipeline error: {str(e)}")
