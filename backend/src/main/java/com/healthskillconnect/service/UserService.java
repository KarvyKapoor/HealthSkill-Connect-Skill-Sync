package com.healthskillconnect.service;

import com.healthskillconnect.model.AppUser;
import com.healthskillconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private static final Pattern PASS = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public AppUser byEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
    }

    public AppUser byId(String id) {
        if (id == null) return null;
        return userRepository.findById(id).orElse(null);
    }

    public boolean matches(AppUser u, String password) {
        return u != null && u.passwordHash != null && encoder.matches(password, u.passwordHash);
    }

    @Transactional
    public AppUser register(String name, String email, String password, String spec, String level, String location) {
        return registerWithRole(name, email, password, "STUDENT", spec, level, location);
    }

    @Transactional
    public AppUser registerWithRole(String name, String email, String password, String role, String spec, String level, String location) {
        if (byEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (!PASS.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be 8+ characters and include uppercase, lowercase, number, and special character.");
        }

        String validRole = (role != null && List.of("STUDENT", "MENTOR", "HEALTHCARE_PROFESSIONAL", "RECRUITER", "ADMIN").contains(role.trim()))
                ? role.trim() : "STUDENT";

        AppUser u = new AppUser();
        u.id = "user-" + UUID.randomUUID().toString().substring(0, 8);
        u.fullName = name.trim();
        u.email = email.trim().toLowerCase();
        u.role = validRole;
        u.specialization = spec != null && !spec.isBlank() ? spec.trim() : "General Healthcare";
        u.experienceLevel = level != null && !level.isBlank() ? level.trim() : "Beginner";
        u.location = location != null ? location.trim() : "";
        u.avatarInitials = AppUser.generateInitials(name);
        u.passwordHash = encoder.encode(password);
        u.skills = new ArrayList<>();
        u.learningInterests = new ArrayList<>();
        u.learningProgress = 0;
        u.onboardingCompleted = false;
        u.verificationStatus = "STUDENT".equals(validRole) ? "VERIFIED" : "UNVERIFIED";
        u.createdAt = LocalDateTime.now();
        u.updatedAt = LocalDateTime.now();

        return userRepository.save(u);
    }

    public List<AppUser> all() {
        return userRepository.findAll();
    }

    public List<AppUser> role(String role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public void changeRole(String id, String role) {
        if (!List.of("STUDENT", "MENTOR", "HEALTHCARE_PROFESSIONAL", "RECRUITER", "ADMIN").contains(role)) {
            throw new IllegalArgumentException("Unsupported role.");
        }
        AppUser u = byId(id);
        if (u == null) {
            throw new NoSuchElementException("User not found.");
        }
        u.role = role;
        u.updatedAt = LocalDateTime.now();
        userRepository.save(u);
    }

    @Transactional
    public AppUser updateProfile(String id, Map<String, Object> updates) {
        AppUser u = byId(id);
        if (u == null) {
            throw new NoSuchElementException("User not found.");
        }

        if (updates.containsKey("fullName") && updates.get("fullName") instanceof String val && !val.isBlank()) {
            u.fullName = val.trim();
            u.avatarInitials = AppUser.generateInitials(u.fullName);
        }
        if (updates.containsKey("specialization") && updates.get("specialization") instanceof String val) {
            u.specialization = val.trim();
        }
        if (updates.containsKey("experienceLevel") && updates.get("experienceLevel") instanceof String val) {
            u.experienceLevel = val.trim();
        }
        if (updates.containsKey("location") && updates.get("location") instanceof String val) {
            u.location = val.trim();
        }
        if (updates.containsKey("headline") && updates.get("headline") instanceof String val) {
            u.headline = val.trim();
        }
        if (updates.containsKey("bio") && updates.get("bio") instanceof String val) {
            u.bio = val.trim();
        }
        if (updates.containsKey("institution") && updates.get("institution") instanceof String val) {
            u.institution = val.trim();
        }
        if (updates.containsKey("degree") && updates.get("degree") instanceof String val) {
            u.degree = val.trim();
        }
        if (updates.containsKey("credentialsSummary") && updates.get("credentialsSummary") instanceof String val) {
            u.credentialsSummary = val.trim();
        }
        if (updates.containsKey("learningProgress") && updates.get("learningProgress") instanceof Number num) {
            u.learningProgress = Math.min(100, Math.max(0, num.intValue()));
        }
        if (updates.containsKey("skills") && updates.get("skills") instanceof List<?> list) {
            u.skills = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && !s.isBlank()) {
                    u.skills.add(s.trim());
                }
            }
        }
        if (updates.containsKey("learningInterests") && updates.get("learningInterests") instanceof List<?> list) {
            u.learningInterests = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && !s.isBlank()) {
                    u.learningInterests.add(s.trim());
                }
            }
        }
        if (updates.containsKey("onboardingCompleted") && updates.get("onboardingCompleted") instanceof Boolean b) {
            u.onboardingCompleted = b;
        }

        u.updatedAt = LocalDateTime.now();
        return userRepository.save(u);
    }

    @Transactional
    public AppUser save(AppUser user) {
        user.updatedAt = LocalDateTime.now();
        return userRepository.save(user);
    }
}
