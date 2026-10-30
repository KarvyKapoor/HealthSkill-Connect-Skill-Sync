package com.healthskillconnect.controller;

import com.healthskillconnect.data.JsonDataLoader;
import com.healthskillconnect.model.AppUser;
import com.healthskillconnect.service.AiClient;
import com.healthskillconnect.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final AiClient ai;
    private final JsonDataLoader data;
    private final UserService users;

    public ApiController(AiClient ai, JsonDataLoader data, UserService users) {
        this.ai = ai;
        this.data = data;
        this.users = users;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "java-backend");
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(HttpServletRequest request) {
        return authenticatedUser(request).publicMap();
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> courses() {
        return data.courses;
    }

    @GetMapping("/paths")
    public List<Map<String, Object>> learningPaths() {
        return data.learningPaths;
    }

    @GetMapping("/opportunities")
    public List<Map<String, Object>> opportunities() {
        return data.opportunities;
    }

    @GetMapping("/resources")
    public List<Map<String, Object>> resources() {
        return data.resources;
    }

    @GetMapping("/dashboard/student")
    public Map<String, Object> studentDashboard(HttpServletRequest request) {
        requireRole(request, "STUDENT");
        return Map.of(
                "courses", data.courses,
                "paths", data.learningPaths,
                "opportunities", data.opportunities
        );
    }

    @GetMapping("/dashboard/mentor")
    public Map<String, Object> mentorDashboard(HttpServletRequest request) {
        AppUser mentorUser = requireRole(request, "MENTOR");
        Map<String, Object> mentor = data.mentors.stream()
                .filter(candidate -> mentorUser.id.equals(candidate.get("userId")))
                .findFirst()
                .orElse(Map.of());

        Set<String> menteeIds = new HashSet<>();
        Object configuredMentees = mentor.get("mentees");
        if (configuredMentees instanceof List<?> ids) {
            for (Object id : ids) {
                if (id instanceof String value) {
                    menteeIds.add(value);
                }
            }
        }

        List<Map<String, Object>> mentees = users.all().stream()
                .filter(user -> menteeIds.contains(user.id))
                .map(AppUser::publicMap)
                .toList();

        List<Map<String, Object>> sessions = new ArrayList<>();
        Object configuredSessions = mentor.get("sessions");
        if (configuredSessions instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> session) {
                    Map<String, Object> safeSession = new java.util.LinkedHashMap<>();
                    session.forEach((key, value) -> {
                        if (key instanceof String name && value != null) {
                            safeSession.put(name, value);
                        }
                    });
                    sessions.add(safeSession);
                }
            }
        }

        Object pendingReviews = mentor.get("pendingReviews");
        return Map.of(
                "mentees", mentees,
                "sessions", sessions,
                "pendingReviews", pendingReviews instanceof Number ? pendingReviews : 0
        );
    }

    @GetMapping("/dashboard/admin")
    public Map<String, Object> adminDashboard(HttpServletRequest request) {
        requireRole(request, "ADMIN");
        List<Map<String, Object>> publicUsers = users.all().stream()
                .map(AppUser::publicMap)
                .toList();
        long students = users.role("STUDENT").size();
        long mentors = users.role("MENTOR").size();

        return Map.of(
                "users", publicUsers,
                "counts", Map.of("students", students, "mentors", mentors),
                "courses", data.courses
        );
    }

    @PatchMapping("/admin/users/{id}/role")
    public Map<String, Object> changeUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        requireRole(request, "ADMIN");
        String role = body == null ? null : body.get("role");
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        users.changeRole(id, role.trim());
        return users.byId(id).publicMap();
    }

    @GetMapping("/ai/health")
    public Map<String, Object> aiHealth() {
        return ai.get("/health");
    }

    @GetMapping("/research")
    public Map<String, Object> research(
            @RequestParam(name = "q", required = false) String query
    ) {
        if (query == null || query.trim().length() < 2 || query.length() > 180) {
            throw new IllegalArgumentException(
                    "Search query must contain 2 to 180 characters."
            );
        }
        String path = UriComponentsBuilder.fromPath("/research")
                .queryParam("q", query.trim())
                .build()
                .encode()
                .toUriString();
        return ai.get(path);
    }

    @PostMapping("/ai/roadmap")
    public Map<String, Object> roadmap(@RequestBody Map<String, Object> body) {
        validateBody(body);
        Object goal = body.get("goal");
        if (!(goal instanceof String goalText) || goalText.trim().length() < 3) {
            throw new IllegalArgumentException(
                    "The goal field must contain at least 3 characters."
            );
        }
        return ai.call("/roadmap", body);
    }

    @PostMapping("/ai/notes")
    public Map<String, Object> notes(@RequestBody Map<String, Object> body) {
        validateBody(body);
        return ai.call("/notes", body);
    }

    @PostMapping("/ai/quiz")
    public Map<String, Object> quiz(@RequestBody Map<String, Object> body) {
        validateBody(body);
        return ai.call("/quiz", body);
    }

    @PostMapping("/ai/presentation")
    public ResponseEntity<byte[]> presentation(
            @RequestBody Map<String, Object> body
    ) {
        validateBody(body);
        byte[] bytes = ai.presentation(body);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=healthskill-presentation.pptx"
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                ))
                .body(bytes);
    }

    private AppUser requireRole(HttpServletRequest request, String role) {
        AppUser user = authenticatedUser(request);
        if (!role.equals(user.role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this resource."
            );
        }
        return user;
    }

    private AppUser authenticatedUser(HttpServletRequest request) {
        Object user = request.getAttribute("authUser");
        if (user instanceof AppUser appUser) {
            return appUser;
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required."
        );
    }

    private void validateBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Request body must not be empty.");
        }
    }
}
