from app.schemas import RoadmapRequest, PresentationRequest
from app.roadmap_service import generate_roadmap
from app.notes_service import generate_notes
from app.presentation_service import build_presentation
from pptx import Presentation
from io import BytesIO

def test_roadmap_week_count_and_fallback_label():
    result=generate_roadmap(RoadmapRequest(goal='Learn epidemiology',specialization='Public Health',duration_weeks=3))
    assert len(result['weeks'])==3 and result['provider']=='rule-based-fallback'
def test_notes_have_revision_and_self_test():
    result=generate_notes('Patient safety','Beginner')
    assert result['label']=='Generated study aid' and result['selfTest']
def test_presentation_is_real_pptx():
    data=build_presentation(PresentationRequest(topic='Patient safety',slide_count=5))
    prs=Presentation(BytesIO(data)); assert len(prs.slides)==5
