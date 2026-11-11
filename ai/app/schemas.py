from pydantic import BaseModel, Field
from typing import List, Optional

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

class StudyPlanRequest(BaseModel):
    target_career: str = Field(min_length=2, max_length=200)
    hours_per_week: int = Field(default=10, ge=2, le=50)
    current_level: str = Field(default="Beginner", max_length=80)
    key_topics: List[str] = Field(default_factory=list)

class ExplainRequest(BaseModel):
    concept: str = Field(min_length=2, max_length=300)
    depth: str = Field(default="Intermediate", max_length=80) # Beginner, Intermediate, Advanced

class SkillPlanRequest(BaseModel):
    target_career: str = Field(min_length=2, max_length=200)
    missing_skills: List[str] = Field(default_factory=list)
    current_qualification: Optional[str] = Field(default="Healthcare Student")
