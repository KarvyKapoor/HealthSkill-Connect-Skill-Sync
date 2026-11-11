from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import Response
from .schemas import (
    RoadmapRequest, NotesRequest, PresentationRequest, QuizRequest,
    StudyPlanRequest, ExplainRequest, SkillPlanRequest
)
from .roadmap_service import generate_roadmap
from .notes_service import generate_notes
from .presentation_service import build_presentation
from .research_service import search_pubmed

app = FastAPI(
    title="HealthSkill Connect AI Service",
    version="0.2.0",
    description="Educational AI helpers, roadmaps, study plans, explainer, and PubMed discovery"
)

@app.get('/health')
def health():
    return {
        "status": "ok",
        "service": "python-ai",
        "llmConfigured": False,
        "capabilities": ["notes", "roadmaps", "presentations", "quizzes", "study-plans", "explainer", "skill-gap-plans", "pubmed"]
    }

@app.post('/roadmap')
def roadmap(req: RoadmapRequest):
    return generate_roadmap(req)

@app.post('/notes')
def notes(req: NotesRequest):
    return generate_notes(req.topic, req.level)

@app.post('/quiz')
def quiz(req: QuizRequest):
    return {
        "provider": "rule-based-fallback",
        "topic": req.topic,
        "questions": [
            {
                "question": f"Which is the primary guideline when studying {req.topic} in clinical training?",
                "options": [
                    "Consult peer-reviewed sources and current official guidelines",
                    "Rely exclusively on unverified informal summaries",
                    "Disregard baseline definitions",
                    "Avoid contextual patient safety considerations"
                ],
                "answer": "Consult peer-reviewed sources and current official guidelines",
                "explanation": "High-quality healthcare practice relies on verified, peer-reviewed evidence and standard operating protocols."
            } for _ in range(req.count)
        ],
        "disclaimer": "Revision questions are educational aids and should be cross-referenced with syllabus standards."
    }

@app.post('/study-plan')
def study_plan(req: StudyPlanRequest):
    daily_hours = max(1.0, round(req.hours_per_week / 5.0, 1))
    topics = req.key_topics if req.key_topics else [f"Foundations of {req.target_career}", "Standard Clinical Workflows", "Diagnostic & Technical Competencies", "Regulatory & Safety Protocols", "Practical Case Review"]
    
    schedule = []
    days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
    for i, day in enumerate(days):
        topic = topics[i % len(topics)]
        schedule.append({
            "day": day,
            "allocatedHours": daily_hours,
            "focusTopic": topic,
            "activities": [
                f"Review 45 min core concept notes on {topic}",
                f"Work through 1 case study application related to {req.target_career}",
                "Complete active recall self-test (15 min)"
            ],
            "milestone": f"Summarize key takeaway for {topic}"
        })
    
    return {
        "provider": "rule-based-study-planner",
        "targetCareer": req.target_career,
        "hoursPerWeek": req.hours_per_week,
        "dailyHours": daily_hours,
        "currentLevel": req.current_level,
        "schedule": schedule,
        "recommendation": f"Focusing {req.hours_per_week} hours/week will establish strong competency for {req.target_career} within 8 to 12 weeks."
    }

@app.post('/explain')
def explain_concept(req: ExplainRequest):
    depth = req.depth.lower()
    if depth == "beginner":
        explanation = f"{req.concept} explained simply: Think of it as a foundational healthcare concept. At its core, it describes a structured way to understand clinical function, patient care, or healthcare systems."
        key_points = [
            f"What it is: Basic definition and core purpose of {req.concept}",
            "Why it matters: Helps healthcare practitioners maintain consistency and quality",
            "Everyday analogy: How it compares to familiar processes"
        ]
        clinical_context = "Used in introductory healthcare coursework and early clinical rotations."
    elif depth == "advanced":
        explanation = f"In-depth analysis of {req.concept}: This concept involves complex pathophysiological, biochemical, or institutional mechanisms that govern outcomes."
        key_points = [
            f"Mechanistic basis and diagnostic implications of {req.concept}",
            "Interactions with standard clinical protocols, contraindications, or system bottlenecks",
            "Critical appraisal of clinical trial data and recent empirical evidence"
        ]
        clinical_context = "Applied in specialized clinical decision-making, hospital audits, or advanced research design."
    else: # Intermediate
        explanation = f"Core operational understanding of {req.concept}: Balances conceptual definitions with practical application in hospital and clinical settings."
        key_points = [
            f"Primary clinical indication and mechanism of {req.concept}",
            "Standard workflow steps and common error vectors",
            "Best practice guidelines endorsed by healthcare authorities"
        ]
        clinical_context = "Crucial for mid-level healthcare students and junior clinical residents."
    
    return {
        "provider": "healthcare-concept-explainer",
        "concept": req.concept,
        "depth": req.depth,
        "explanation": explanation,
        "keyPoints": key_points,
        "clinicalContext": clinical_context,
        "disclaimer": "Educational explanation for learning purposes only; not a substitute for formal clinical textbooks or medical judgment."
    }

@app.post('/skill-plan')
def skill_improvement_plan(req: SkillPlanRequest):
    missing = req.missing_skills if req.missing_skills else ["Core Clinical Knowledge", "Regulatory Guidelines", "Technical Documentation"]
    steps = []
    for i, skill in enumerate(missing):
        steps.append({
            "step": i + 1,
            "skill": skill,
            "estimatedWeeks": 2,
            "recommendedResources": [
                f"WHO / National standard guidelines on {skill}",
                f"HealthSkill Connect study notes on {skill}",
                "Related PubMed research papers"
            ],
            "practicalProject": f"Draft a 2-page applied clinical scenario demonstration of {skill}",
            "verificationMethod": "Self-assessment quiz + project upload to user profile"
        })
    
    return {
        "provider": "skill-gap-improvement-engine",
        "targetCareer": req.target_career,
        "skillsAddressed": missing,
        "totalEstimatedWeeks": len(steps) * 2,
        "improvementSteps": steps,
        "message": f"Following this targeted {len(steps)*2}-week plan will close your critical skill gaps for {req.target_career}."
    }

@app.post('/presentation')
def presentation(req: PresentationRequest):
    try:
        data = build_presentation(req)
    except Exception as e:
        raise HTTPException(status_code=500, detail="Could not generate presentation.") from e
    return Response(
        data,
        media_type='application/vnd.openxmlformats-officedocument.presentationml.presentation',
        headers={'Content-Disposition': 'attachment; filename="healthskill-presentation.pptx"'}
    )

@app.get('/research')
def research(q: str = Query(min_length=2, max_length=180)):
    try:
        return search_pubmed(q)
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))
