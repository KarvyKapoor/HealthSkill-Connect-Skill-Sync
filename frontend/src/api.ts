const API_BASE_URL = (
  (import.meta as ImportMeta & { env: Record<string, string | undefined> })
    .env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");

// Check localStorage for persisted token on initial load
let accessToken: string | null = (typeof window !== "undefined") ? localStorage.getItem("healthskill_token") : null;

export function setToken(token: string | null): void {
  accessToken = token;
  if (typeof window !== "undefined") {
    if (token) {
      localStorage.setItem("healthskill_token", token);
    } else {
      localStorage.removeItem("healthskill_token");
    }
  }
}

export function getToken(): string | null {
  return accessToken;
}

export interface AuthUser {
  id: string;
  fullName: string;
  email: string;
  role: string;
  specialization?: string;
  experienceLevel?: string;
  location?: string;
  headline?: string;
  bio?: string;
  institution?: string;
  degree?: string;
  avatarInitials?: string;
  verificationStatus?: string;
  credentialsSummary?: string;
  skills?: string[];
  learningInterests?: string[];
  learningProgress?: number;
  onboardingCompleted?: boolean;
  [key: string]: unknown;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role?: string;
  specialization?: string;
  experienceLevel?: string;
  location?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export class ApiError extends Error {
  status: number;
  details: unknown;

  constructor(message: string, status: number, details?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

function normalizePath(path: string): string {
  return path.replace(/^\/+/, "");
}

function getErrorMessage(data: unknown): string {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    const value = data as Record<string, unknown>;

    for (const key of ["message", "detail", "error", "title"]) {
      const message = value[key];
      if (typeof message === "string" && message.trim()) {
        return message;
      }
    }

    if (Array.isArray(value.errors)) {
      return value.errors
        .map((error) => (typeof error === "string" ? error : JSON.stringify(error)))
        .join("; ");
    }
  }

  return "The request failed. Please try again.";
}

export async function api<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const endpoint = `${API_BASE_URL}/${normalizePath(path)}`;
  const headers = new Headers(options.headers);

  headers.set("Accept", "application/json");

  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  if (options.body && !isFormData && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const normalized = normalizePath(path);
  const isAuthEndpoint = normalized === "auth/login" || normalized === "auth/register";

  const token = getToken();
  if (token && !isAuthEndpoint) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(endpoint, {
      ...options,
      headers,
    });
  } catch {
    throw new ApiError(
      `Cannot reach the backend at ${endpoint}. Make sure Spring Boot is running on port 8080.`,
      0
    );
  }

  const contentType = response.headers.get("content-type") || "";
  let data: unknown = null;

  if (response.status !== 204) {
    try {
      data = contentType.includes("application/json")
        ? await response.json()
        : await response.text();
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    if (response.status === 401 && !isAuthEndpoint) {
      setToken(null);
    }

    throw new ApiError(
      `${response.status} ${response.statusText}: ${getErrorMessage(data)}`,
      response.status,
      data
    );
  }

  if (
    isAuthEndpoint &&
    data &&
    typeof data === "object" &&
    "token" in data &&
    typeof (data as AuthResponse).token === "string"
  ) {
    setToken((data as AuthResponse).token);
  }

  return data as T;
}

export async function login(credentials: LoginRequest): Promise<AuthResponse> {
  return api<AuthResponse>("auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });
}

export async function register(details: RegisterRequest): Promise<AuthResponse> {
  return api<AuthResponse>("auth/register", {
    method: "POST",
    body: JSON.stringify(details),
  });
}

export function logout(): void {
  setToken(null);
}

export async function getCurrentUser(): Promise<AuthUser> {
  return api<AuthUser>("me");
}

// Onboarding
export async function submitOnboarding(data: Record<string, unknown>): Promise<any> {
  return api("onboarding/submit", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getLatestOnboarding(): Promise<any> {
  return api("onboarding/latest");
}

// Profile
export async function updateProfile(updates: Record<string, unknown>): Promise<AuthUser> {
  return api<AuthUser>("users/profile", {
    method: "PATCH",
    body: JSON.stringify(updates),
  });
}

// Skills & Careers
export async function getSkills(): Promise<any[]> {
  return api("skills");
}

export async function getUserSkills(): Promise<any[]> {
  return api("users/skills");
}

export async function addUserSkill(data: { name: string; proficiency?: string; evidenceType?: string; evidenceTitle?: string }): Promise<any> {
  return api("users/skills", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function deleteUserSkill(id: number): Promise<any> {
  return api(`users/skills/${id}`, { method: "DELETE" });
}

export async function getCareers(): Promise<any[]> {
  return api("careers");
}

export async function getCareerRecommendations(): Promise<any[]> {
  return api("careers/recommendations");
}

export async function getMarketReadiness(): Promise<any> {
  return api("market-readiness");
}

// Opportunities
export async function getOpportunities(type?: string): Promise<any[]> {
  const q = type && type !== "ALL" ? `?type=${encodeURIComponent(type)}` : "";
  return api(`opportunities${q}`);
}

export async function postOpportunity(data: Record<string, unknown>): Promise<any> {
  return api("opportunities", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function toggleSaveOpportunity(id: string): Promise<any> {
  return api(`opportunities/${id}/save`, { method: "POST" });
}

export async function applyOpportunity(id: string, data: { applicantName?: string; applicantEmail?: string; applicantSpecialization?: string; coverNote?: string }): Promise<any> {
  return api(`opportunities/${id}/apply`, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getMyApplications(): Promise<any[]> {
  return api("opportunities/my-applications");
}

// Networking
export async function getNetworkFeed(category?: string): Promise<any[]> {
  const q = category && category !== "ALL" ? `?category=${encodeURIComponent(category)}` : "";
  return api(`network/feed${q}`);
}

export async function createPost(data: { title: string; content: string; category: string; tags?: string[] }): Promise<any> {
  return api("network/posts", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function reactPost(id: number): Promise<any> {
  return api(`network/posts/${id}/react`, { method: "POST" });
}

export async function addComment(id: number, content: string): Promise<any> {
  return api(`network/posts/${id}/comments`, {
    method: "POST",
    body: JSON.stringify({ content }),
  });
}

export async function getConnections(): Promise<any[]> {
  return api("network/connections");
}

export async function sendConnectionRequest(recipientId: string): Promise<any> {
  return api(`network/connect/${recipientId}`, { method: "POST" });
}

export async function updateConnection(id: number, action: "ACCEPT" | "DECLINE"): Promise<any> {
  return api(`network/connections/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ action }),
  });
}

export async function getDiscoverableMembers(query?: string, role?: string): Promise<any[]> {
  const params = new URLSearchParams();
  if (query) params.set("q", query);
  if (role && role !== "ALL") params.set("role", role);
  const qs = params.toString() ? `?${params.toString()}` : "";
  return api(`network/members${qs}`);
}

export async function requestMentorship(data: { mentorId: string; subject: string; goals?: string; message?: string }): Promise<any> {
  return api("network/mentorship-request", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getMentorshipRequests(asMentor = false): Promise<any[]> {
  return api(`network/mentorship-requests?asMentor=${asMentor}`);
}

export async function updateMentorshipRequest(id: number, status: string, notes?: string): Promise<any> {
  return api(`network/mentorship-requests/${id}`, {
    method: "PATCH",
    body: JSON.stringify({ status, notes }),
  });
}

// Content & AI Studio
export async function getUserNotes(): Promise<any[]> {
  return api("content/notes");
}

export async function createNote(data: Record<string, unknown>): Promise<any> {
  return api("content/notes", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateNote(id: number, updates: Record<string, unknown>): Promise<any> {
  return api(`content/notes/${id}`, {
    method: "PATCH",
    body: JSON.stringify(updates),
  });
}

export async function deleteNote(id: number): Promise<any> {
  return api(`content/notes/${id}`, { method: "DELETE" });
}

export async function generateRoadmap(request: Record<string, unknown>): Promise<any> {
  return api("ai/roadmap", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function saveRoadmap(data: Record<string, unknown>): Promise<any> {
  return api("ai/save-roadmap", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getUserRoadmaps(): Promise<any[]> {
  return api("content/roadmaps");
}

export async function generateStudyPlan(data: Record<string, unknown>): Promise<any> {
  return api("ai/study-plan", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function saveStudyPlan(data: Record<string, unknown>): Promise<any> {
  return api("ai/save-study-plan", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getUserStudyPlans(): Promise<any[]> {
  return api("content/study-plans");
}

export async function explainConcept(data: { concept: string; depth: string }): Promise<any> {
  return api("ai/explain", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function generateSkillPlan(data: { target_career: string; missing_skills: string[] }): Promise<any> {
  return api("ai/skill-plan", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function generateNotes(request: Record<string, unknown>): Promise<any> {
  return api("ai/notes", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function generateQuiz(request: Record<string, unknown>): Promise<any> {
  return api("ai/quiz", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function searchResearch(query: string): Promise<any> {
  return api(`research?q=${encodeURIComponent(query)}`);
}

export async function getAuditLogs(): Promise<any[]> {
  return api("admin/audit-logs");
}

export async function changeUserRole(userId: string, role: string): Promise<any> {
  return api(`admin/users/${userId}/role`, {
    method: "PATCH",
    body: JSON.stringify({ role }),
  });
}

export async function generatePresentation(request: Record<string, unknown>): Promise<Blob> {
  const endpoint = `${API_BASE_URL}/ai/presentation`;
  const headers = new Headers({
    "Content-Type": "application/json",
    Accept: "application/vnd.openxmlformats-officedocument.presentationml.presentation",
  });

  const token = getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(endpoint, {
      method: "POST",
      headers,
      body: JSON.stringify(request),
    });
  } catch {
    throw new ApiError("Cannot connect to the backend.", 0);
  }

  if (!response.ok) {
    const contentType = response.headers.get("content-type") || "";
    let message = `${response.status} ${response.statusText}`;
    try {
      const errorData = contentType.includes("application/json")
        ? await response.json()
        : await response.text();
      message = getErrorMessage(errorData);
    } catch {}
    throw new ApiError(message, response.status);
  }

  return response.blob();
}
