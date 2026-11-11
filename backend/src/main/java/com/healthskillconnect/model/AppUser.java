package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @Column(nullable = false, length = 64)
    public String id;

    @Column(nullable = false, length = 120)
    public String fullName;

    @Column(nullable = false, unique = true, length = 180)
    public String email;

    @Column(nullable = false, length = 40)
    public String role; // STUDENT, MENTOR, HEALTHCARE_PROFESSIONAL, RECRUITER, ADMIN

    @Column(length = 120)
    public String specialization;

    @Column(length = 80)
    public String experienceLevel;

    @Column(length = 120)
    public String location;

    @Column(length = 10)
    public String avatarInitials;

    @Column(length = 255)
    public String passwordHash;

    @Transient
    public String password;

    @Column(length = 255)
    public String headline;

    @Column(length = 1000)
    public String bio;

    @Column(length = 180)
    public String institution;

    @Column(length = 120)
    public String degree;

    @Column(length = 40)
    public String verificationStatus = "UNVERIFIED"; // UNVERIFIED, PENDING, VERIFIED

    @Column(length = 500)
    public String credentialsSummary;

    public int learningProgress = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills_list", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill_name")
    public List<String> skills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests_list", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    public List<String> learningInterests = new ArrayList<>();

    public boolean onboardingCompleted = false;

    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();

    public AppUser() {}

    public AppUser(String id, String fullName, String email, String role, String specialization, String experienceLevel, String location) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.specialization = specialization;
        this.experienceLevel = experienceLevel;
        this.location = location;
        this.avatarInitials = generateInitials(fullName);
    }

    public static String generateInitials(String name) {
        if (name == null || name.isBlank()) return "HC";
        return Arrays.stream(name.trim().split("\\s+"))
                .limit(2)
                .map(s -> s.isEmpty() ? "" : s.substring(0, 1).toUpperCase())
                .reduce("", String::concat);
    }

    public Map<String, Object> publicMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("fullName", fullName);
        m.put("email", email);
        m.put("role", role);
        m.put("specialization", specialization);
        m.put("experienceLevel", experienceLevel);
        m.put("location", location);
        m.put("avatarInitials", avatarInitials);
        m.put("headline", headline);
        m.put("bio", bio);
        m.put("institution", institution);
        m.put("degree", degree);
        m.put("verificationStatus", verificationStatus);
        m.put("credentialsSummary", credentialsSummary);
        m.put("skills", skills != null ? skills : Collections.emptyList());
        m.put("learningInterests", learningInterests != null ? learningInterests : Collections.emptyList());
        m.put("learningProgress", learningProgress);
        m.put("onboardingCompleted", onboardingCompleted);
        m.put("createdAt", createdAt != null ? createdAt.toString() : null);
        return m;
    }
}
