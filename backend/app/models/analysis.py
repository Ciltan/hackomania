from sqlalchemy import Column, Integer, String, JSON, Text
from core.database import Base

class AnalysisRecord(Base):
    __tablename__ = "analysis_records"

    id = Column(Integer, primary_key=True, index=True)
    input_hash = Column(String, unique=True, index=True) # Hash of URL or Text to cache
    url = Column(String, nullable=True)
    text_content = Column(Text, nullable=True)
    response_json = Column(JSON, nullable=False) # Store the generated AnalysisResponse as JSON
