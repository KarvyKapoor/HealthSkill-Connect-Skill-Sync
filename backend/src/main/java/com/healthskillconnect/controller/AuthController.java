
package com.healthskillconnect.controller;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthskillconnect.model.AppUser;
import com.healthskillconnect.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final com.healthskillconnect.security.JwtService jwt;

    public AuthController(UserService users, com.healthskillconnect.security.JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required.")
            );
        }

        AppUser user = users.byEmail(email.trim());

        if (user == null || !users.matches(user, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        return ResponseEntity.ok(Map.of(
                "token", jwt.create(user),
                "user", user.publicMap()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> body) {

        try {
            String name = required(body, "name");
            String email = required(body, "email");
            String password = required(body, "password");

            String specialization =
                    optional(body, "specialization", "General");
            String experienceLevel =
                    optional(body, "experienceLevel", "Beginner");
            String location = optional(body, "location", "");

            AppUser user = users.register(
                    name,
                    email,
                    password,
                    specialization,
                    experienceLevel,
                    location
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "token", jwt.create(user),
                    "user", user.publicMap()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            e.getMessage() == null
                                    ? "Invalid registration details."
                                    : e.getMessage())
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

    private String optional(
            Map<String, String> body,
            String key,
            String defaultValue) {

        String value = body.get(key);
        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }
}
