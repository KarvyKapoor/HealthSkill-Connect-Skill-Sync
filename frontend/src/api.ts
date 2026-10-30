
const API_BASE_URL = (
  (import.meta as ImportMeta & { env: Record<string, string | undefined> })
    .env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");

let accessToken: string | null = null;

export function setToken(token: string | null): void {
  accessToken = token;
}

export interface AuthUser {
  id: string;
  fullName?: string;
  email?: string;
  role?: string;
  specialization?: string;
  experienceLevel?: string;
  location?: string;
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
  specialization?: string;
  experienceLevel?: string;
  location?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RoadmapRequest {
  goal: string;
  specialization: string;
  experience_level: string;
  interests: string[];
  duration_weeks: number;
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
        .map((error) =>
          typeof error === "string" ? error : JSON.stringify(error)
        )
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

  const isFormData =
    typeof FormData !== "undefined" && options.body instanceof FormData;

  if (options.body && !isFormData && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const normalized = normalizePath(path);
  const isAuthEndpoint =
    normalized === "auth/login" || normalized === "auth/register";

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

export async function login(
  credentials: LoginRequest
): Promise<AuthResponse> {
  return api<AuthResponse>("auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });
}

export async function register(
  details: RegisterRequest
): Promise<AuthResponse> {
  return api<AuthResponse>("auth/register", {
    method: "POST",
    body: JSON.stringify(details),
  });
}

export function logout(): void {
  setToken(null);
}

export function getToken(): string | null {
  return accessToken;
}

export async function getBackendHealth(): Promise<unknown> {
  return api("health");
}

export async function getAiHealth(): Promise<unknown> {
  return api("ai/health");
}

export async function generateRoadmap(
  request: RoadmapRequest
): Promise<unknown> {
  return api("ai/roadmap", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function generateNotes(
  request: Record<string, unknown>
): Promise<unknown> {
  return api("ai/notes", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function generateQuiz(
  request: Record<string, unknown>
): Promise<unknown> {
  return api("ai/quiz", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function generatePresentation(
  request: Record<string, unknown>
): Promise<Blob> {
  const endpoint = `${API_BASE_URL}/ai/presentation`;
  const headers = new Headers({
    "Content-Type": "application/json",
    Accept: "application/octet-stream",
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
    } catch {
      // Keep the HTTP status message.
    }

    throw new ApiError(message, response.status);
  }

  return response.blob();
}
