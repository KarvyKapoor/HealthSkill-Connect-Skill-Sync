# HealthSkill Connect

A runnable MVP for healthcare learning and career development. It uses React + TypeScript + Vite for the UI, Java 21 + Spring Boot as the main API, and a separate Python FastAPI service for learning helpers and PubMed discovery. **No database is configured or connected.** Dummy JSON files are loaded at Java startup; new registrations and learning state are in memory and disappear when the backend restarts.

## Architecture

```text
React + TypeScript (5173)
        | REST + in-memory bearer token
        v
Spring Boot Java API (8080) ---- HTTP ----> FastAPI AI service (8000)
        |                                      |-- rule-based study aids
        |-- JSON dummy data                    |-- python-pptx files
        |-- in-memory user registry            |-- NCBI PubMed E-utilities
        `-- BCrypt + JWT

No database connection, ORM, migration, or persistence layer is enabled.
```

## Requirements

- Java 21 or newer and Maven 3.9+
- Python 3.11+
- Node.js 20+ and npm
- Network access is needed for npm/pip dependency installation and PubMed searches.

## Demo accounts

All demo accounts use password `DemoPass123!`:

| Role | Email |
|---|---|
| Student | `aarav.student@example.com` |
| Student | `meera.student@example.com` |
| Mentor | `nisha.mentor@example.com` |
| Mentor | `arjun.mentor@example.com` |
| Admin | `admin@example.com` |

Additional student accounts are in `backend/src/main/resources/data/dummy-users.json`. Demo passwords in that JSON are development-only seed values; Java hashes them with BCrypt during startup and never returns hashes in API responses. Change these before any shared demo. These credentials and the local JWT default are **not production-ready**.

## Start each service

Run all three services in separate terminals from the repository root.

### 1. Python AI service

```bash
cd ai
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# macOS/Linux: source .venv/bin/activate
python -m pip install -r requirements.txt
# Optional: copy .env.example to .env and edit NCBI_EMAIL if desired
uvicorn app.main:app --reload --port 8000
```

Health: http://localhost:8000/health and docs: http://localhost:8000/docs.

### 2. Java backend

```bash
cd backend
# Set a local JWT secret (at least 32 bytes). PowerShell:
$env:JWT_SECRET = "local-dev-secret-replace-with-random-32-bytes"
$env:AI_SERVICE_URL = "http://localhost:8000"
$env:FRONTEND_ORIGIN = "http://localhost:5173"
./mvnw spring-boot:run
```

This archive does not include Maven Wrapper binaries/scripts, so if `./mvnw` is unavailable use `mvn spring-boot:run` (Windows: `mvn spring-boot:run`). Health: http://localhost:8080/api/health.

### 3. React frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Optional frontend setting: copy `frontend/.env.example` to `.env` and set `VITE_API_BASE_URL=http://localhost:8080/api`.

## Implemented workflows

- Login and student registration with frontend and backend validation, BCrypt hashes, JWT bearer auth, and logout.
- Public sign-up always assigns `STUDENT`; role changes are restricted to the admin endpoint.
- Separate student, mentor, and admin dashboard API responses and frontend views.
- JSON-backed courses, learning pathways, opportunities, resources, and mentor sample sessions.
- In-memory account registrations and in-memory learning profile values, reset on backend restart.
- AI roadmap, notes, and quiz endpoints with an explicit rule-based fallback label.
- Real `.pptx` generation via `python-pptx` and download through Java proxy.
- PubMed search via NCBI E-utilities, returning actual result IDs/titles/dates/official URLs.
- Responsive navigation, profile, learning resources, opportunities, and accessible form labels/focus states.

## API endpoints

- `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/me`
- `GET /api/dashboard/student`, `/api/dashboard/mentor`, `/api/dashboard/admin` (role-restricted)
- `GET /api/courses`, `/api/paths`, `/api/opportunities`, `/api/resources`
- `PATCH /api/admin/users/{id}/role` (Admin only)
- `POST /api/ai/roadmap`, `/api/ai/notes`, `/api/ai/quiz`, `/api/ai/presentation`
- `GET /api/ai/health`, `GET /api/research?q=...` (authenticated; proxied to PubMed)
- Python service endpoints are documented at `/docs`.

Run the Python service tests from `ai/` with `python -m pytest -q`.

## Configuration and security notes

- `backend/.env.example`, `frontend/.env.example`, and `ai/.env.example` show placeholders only. Spring reads environment variables directly; create shell environment variables before launch.
- `JWT_SECRET` must be at least 32 bytes. The default in `application.yml` is only for local development. Use a strong random secret locally.
- The frontend keeps the access token in JavaScript memory, not local/session storage. Refreshing the page signs the user out. This avoids persistent token storage but is not a full secure-cookie production architecture.
- JWT signatures and expiry are checked; authorization is checked server-side for dashboard and role-management routes. The UI role is not authorization proof.
- CORS is limited to the configured frontend origin. Do not expose the development server publicly.
- Demo seed passwords are intentionally demo-only. No real secrets should be committed.
- This is an educational MVP, not a production-ready identity or healthcare system. Add rate limiting, audit logging, secure cookie/CSRF strategy if desired, security review, monitoring, and formal data protection controls before production.
- Roadmaps, notes, and quizzes use deterministic rule-based fallbacks. No real LLM provider adapter is enabled, even if placeholder environment variables are set.
- Research search requires outbound network access. If NCBI is unavailable, the AI API returns a clear service error.
- The UI's learning progress is sample profile data. A full module completion workflow and durable saved items are not implemented. Admin role mutation is implemented; content mutation is not.

## Dummy data and future database

Edit JSON files under `backend/src/main/resources/data/`. Malformed or missing files cause an understandable startup failure. The future data model proposal is in `database/SCHEMA.md`. No database driver, ORM, connection string, migrations, or database server is used.

## Troubleshooting

- **401 after refresh:** expected, the bearer token is memory-only; log in again.
- **AI service unavailable:** start FastAPI on port 8000 and check `AI_SERVICE_URL`.
- **JWT secret error:** set `JWT_SECRET` to a string of at least 32 bytes.
- **Port busy:** change service ports and update the corresponding URLs.
- **PubMed returns 503:** verify network access and retry.
- **npm install fails:** check Node/npm versions and network access.
- **Maven build fails:** verify Java 21 and Maven availability.

See `docs/ARCHITECTURE.md`, `docs/SECURITY.md`, and `database/SCHEMA.md` for additional detail.
