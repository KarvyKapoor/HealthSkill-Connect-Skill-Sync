# Python AI service

Python 3.11+. Install with `python -m pip install -r requirements.txt`, then run `uvicorn app.main:app --reload --port 8000` from this folder. Swagger UI: http://localhost:8000/docs.

Roadmap, notes, and quiz currently use clearly labelled rule-based fallback logic. No LLM is called, and the health endpoint reports this. A provider adapter can be added later using environment variables in `.env.example`. PubMed discovery uses NCBI E-utilities and real result IDs/URLs. Network availability is required for research search. Presentation generation returns a real PowerPoint file.

Run tests from this folder: `python -m pytest -q`. Outputs are educational aids, not medical advice.
