from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.endpoints import router as analysis_router
from core.database import Base, engine

# Create the database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Fact-Checking API",
    description="Backend API for verifying claims and URLs with LLM and lateral reading.",
    version="1.0.0"
)

# Allow CORS for the Android app (or web frontend if added later)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analysis_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"status": "ok", "message": "Fact-Checking API is running"}
