package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_responses")
public class OnboardingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(length = 120)
    public String currentQualification; // Undergraduate, Post-graduate, Diploma, Doctorate

    @Column(length = 140)
    public String degreeOrCourse; // MBBS, BSc Nursing, BPharm, BPT, etc.

    @Column(length = 140)
    public String specialization;

    @Column(length = 180)
    public String institution;

    @Column(length = 80)
    public String academicYearOrSemester;

    @Column(columnDefinition = "TEXT")
    public String healthcareInterestsJson; // List of selected interests

    @Column(columnDefinition = "TEXT")
    public String careerPreferencesJson; // List of preferences

    @Column(columnDefinition = "TEXT")
    public String existingSkillsJson; // List of skills with proficiency

    @Column(columnDefinition = "TEXT")
    public String experienceSummary;

    @Column(columnDefinition = "TEXT")
    public String learningPreferencesJson; // e.g. self-paced, video, case-study, mentor-led

    @Column(columnDefinition = "TEXT")
    public String shortTermGoals;

    @Column(columnDefinition = "TEXT")
    public String longTermGoals;

    public LocalDateTime createdAt = LocalDateTime.now();

    public OnboardingResponse() {}
}
