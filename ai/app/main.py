from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import Response
from .schemas import RoadmapRequest, NotesRequest, PresentationRequest, QuizRequest
from .roadmap_service import generate_roadmap
from .notes_service import generate_notes
from .presentation_service import build_presentation
from .research_service import search_pubmed
app=FastAPI(title="HealthSkill Connect AI Service",version="0.1.0",description="Educational AI helpers and PubMed discovery")
@app.get('/health')
def health(): return {"status":"ok","service":"python-ai","llmConfigured":False,"mode":"rule-based fallback unless provider integration is added"}
@app.post('/roadmap')
def roadmap(req:RoadmapRequest): return generate_roadmap(req)
@app.post('/notes')
def notes(req:NotesRequest): return generate_notes(req.topic,req.level)
@app.post('/quiz')
def quiz(req:QuizRequest):
    return {"provider":"rule-based-fallback","topic":req.topic,"questions":[{"question":f"Which is the best first step when studying {req.topic}?","options":["Consult a trusted introductory source","Assume an unverified social post is correct","Skip definitions","Avoid checking evidence"],"answer":"Consult a trusted introductory source","explanation":"Start with reliable material and verify claims."} for _ in range(req.count)],"disclaimer":"Revision questions are educational and should be checked for suitability."}
@app.post('/presentation')
def presentation(req:PresentationRequest):
    try: data=build_presentation(req)
    except Exception as e: raise HTTPException(status_code=500,detail="Could not generate presentation.") from e
    return Response(data,media_type='application/vnd.openxmlformats-officedocument.presentationml.presentation',headers={'Content-Disposition':'attachment; filename="healthskill-presentation.pptx"'})
@app.get('/research')
def research(q:str=Query(min_length=2,max_length=180)):
    try:return search_pubmed(q)
    except ValueError as e:raise HTTPException(status_code=422,detail=str(e))
    except RuntimeError as e:raise HTTPException(status_code=503,detail=str(e))
