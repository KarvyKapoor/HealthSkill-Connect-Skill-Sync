# Architecture notes

The frontend uses a small API client that attaches the in-memory JWT to protected calls. Spring Boot is the main application API, validates DTOs, loads editable JSON data on startup, hashes seed and registered passwords using BCrypt, signs JWTs, and enforces roles. It calls FastAPI over HTTP for AI-oriented features. FastAPI owns all learning helper logic, presentation generation, and PubMed access.

## Request flows

1. Login: React submits credentials to Java. Java verifies BCrypt, issues a signed JWT, and returns a safe user profile.
2. Protected request: React sends bearer token. Java validates signature/expiry, resolves the in-memory user, and checks role in protected handlers.
3. Learning content: Java serves the dummy JSON-backed data.
4. AI request: Java proxies request to FastAPI. Presentation bytes are proxied back as a PPTX download.
5. Research: FastAPI calls NCBI E-utilities, returning official PubMed links and IDs from actual records.

## Deliberate MVP limitations

No database, refresh tokens, cookie auth, full course completion workflow, saved items, production LLM adapter, admin content editor, or live opportunity aggregation. In-memory registrations are lost on restart. Demo users reload from JSON.
