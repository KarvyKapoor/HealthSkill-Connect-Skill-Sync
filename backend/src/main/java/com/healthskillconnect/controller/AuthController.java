package com.healthskillconnect.controller;

import com.healthskillconnect.model.AppUser;
import com.healthskillconnect.security.JwtService;
import com.healthskillconnect.service.AuditService;
import com.healthskillconnect.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final JwtService jwt;
    private final AuditService auditService;

    public AuthController(UserService users, JwtService jwt, AuditService auditService) {
        this.users = users;
        this.jwt = jwt;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required.")
            );
        }

        AppUser user = users.byEmail(email.trim());

        if (user == null || !users.matches(user, password)) {
            auditService.logEvent(
                    user != null ? user.id : null,
                    email.trim(),
                    user != null ? user.role : null,
                    "AUTH_LOGIN",
                    "USER",
                    user != null ? user.id : null,
                    "FAILURE",
                    request,
                    Map.of("reason", "Invalid credentials")
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        auditService.logEvent(
                user.id,
                user.email,
                user.role,
                "AUTH_LOGIN",
                "USER",
                user.id,
                "SUCCESS",
                request,
                Map.of("role", user.role)
        );

        return ResponseEntity.ok(Map.of(
                "token", jwt.create(user),
                "user", user.publicMap()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        try {
            String name = required(body, "name");
            String email = required(body, "email");
            String password = required(body, "password");

            String role = optional(body, "role", "STUDENT");
            String specialization = optional(body, "specialization", "General Healthcare");
            String experienceLevel = optional(body, "experienceLevel", "Beginner");
            String location = optional(body, "location", "");

            AppUser user = users.registerWithRole(
                    name,
                    email,
                    password,
                    role,
                    specialization,
                    experienceLevel,
                    location
            );

            auditService.logEvent(
                    user.id,
                    user.email,
                    user.role,
                    "AUTH_REGISTER",
                    "USER",
                    user.id,
                    "SUCCESS",
                    request,
                    Map.of("role", user.role, "specialization", user.specialization)
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "token", jwt.create(user),
                    "user", user.publicMap()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage() == null ? "Invalid registration details." : e.getMessage())
            );
        }
    }

    private String required(Map<String, String> body, String key) {
        String value = body.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return value.trim();
    }

    private String optional(Map<String, String> body, String key, String defaultValue) {
        String value = body.get(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
