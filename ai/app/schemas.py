from pydantic import BaseModel, Field
from typing import List
class RoadmapRequest(BaseModel):
    goal: str = Field(min_length=3, max_length=500)
    specialization: str = Field(default="Healthcare", max_length=120)
    experience_level: str = Field(default="Beginner", max_length=80)
    interests: List[str] = Field(default_factory=list, max_length=20)
    duration_weeks: int = Field(default=4, ge=1, le=24)
class NotesRequest(BaseModel):
    topic: str = Field(min_length=2, max_length=300)
    level: str = Field(default="Beginner", max_length=80)
class PresentationRequest(BaseModel):
    topic: str = Field(min_length=2, max_length=300)
    title: str = Field(default="Healthcare Study Presentation", max_length=150)
    audience: str = Field(default="Healthcare learners", max_length=150)
    slide_count: int = Field(default=6, ge=3, le=15)
class QuizRequest(BaseModel):
    topic: str = Field(min_length=2, max_length=300)
    count: int = Field(default=5, ge=1, le=15)
