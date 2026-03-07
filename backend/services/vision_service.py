import base64
import tempfile
import os
from core.config import get_openai_client, OPENAI_MODEL


def extract_text_from_image(image_bytes: bytes, mime_type: str = "image/png") -> str:
    """Uses GPT vision to extract and describe the textual content of an image."""
    b64_image = base64.b64encode(image_bytes).decode("utf-8")
    data_url = f"data:{mime_type};base64,{b64_image}"

    client = get_openai_client()
    response = client.chat.completions.create(
        model=OPENAI_MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                    "You are an OCR and content extraction assistant. "
                    "Extract ALL visible text from the image. "
                    "If the image is a screenshot of a news article or social media post, "
                    "return the full article text, the headline, and the source/domain if visible. "
                    "Format your output as:\n"
                    "DOMAIN: <domain if visible, otherwise 'unknown'>\n"
                    "HEADLINE: <headline if visible>\n"
                    "CONTENT: <full extracted text>"
                ),
            },
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "Extract all the text from this image."},
                    {"type": "image_url", "image_url": {"url": data_url}},
                ],
            },
        ],
    )
    return response.choices[0].message.content


def extract_text_from_video(video_bytes: bytes, file_extension: str = ".mp4") -> str:
    """Extracts a keyframe from a video and runs vision extraction on it."""
    try:
        import cv2
        import numpy as np
    except ImportError:
        return "Error: opencv-python is required for video processing. Install it with: pip install opencv-python"

    # Save video to a temp file so OpenCV can read it
    with tempfile.NamedTemporaryFile(suffix=file_extension, delete=False) as tmp:
        tmp.write(video_bytes)
        tmp_path = tmp.name

    try:
        cap = cv2.VideoCapture(tmp_path)
        if not cap.isOpened():
            return "Error: Could not open video file."

        total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        # Grab a frame from 25% into the video (skip intros/black screens)
        target_frame = max(1, total_frames // 4)
        cap.set(cv2.CAP_PROP_POS_FRAMES, target_frame)

        ret, frame = cap.read()
        cap.release()

        if not ret:
            return "Error: Could not read a frame from the video."

        # Encode frame as PNG bytes
        _, buffer = cv2.imencode(".png", frame)
        frame_bytes = buffer.tobytes()

        return extract_text_from_image(frame_bytes, mime_type="image/png")
    finally:
        os.unlink(tmp_path)
