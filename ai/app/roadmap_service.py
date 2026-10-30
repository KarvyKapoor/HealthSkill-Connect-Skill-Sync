def generate_roadmap(req):
    weeks=[]
    themes=["Build foundational vocabulary", "Understand core concepts", "Apply concepts to a case study", "Review and self-assess"]
    for i in range(req.duration_weeks):
        theme=themes[i%len(themes)]
        weeks.append({"week":i+1,"title":f"{req.specialization}: {theme}","objective":f"Explain how {req.goal} relates to {req.specialization.lower()} practice.","activities":[f"Study a trusted introductory resource about {req.goal} for 30 minutes", "Write a short summary in your own words", "Complete a 5-question self-check"],"milestone":f"Share one key insight from week {i+1}"})
    return {"provider":"rule-based-fallback","disclaimer":"Educational planning aid, not clinical guidance. No LLM provider is configured.","goal":req.goal,"durationWeeks":req.duration_weeks,"weeks":weeks}
