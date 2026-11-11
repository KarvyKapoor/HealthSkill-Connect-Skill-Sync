package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @Column(nullable = false, length = 64)
    public String id;

    @Column(nullable = false, length = 180)
    public String title;

    @Column(nullable = false, length = 180)
    public String organization;

    @Column(nullable = false, length = 40)
    public String type; // INTERNSHIP, FULL_TIME, PART_TIME, RESEARCH, CLINICAL_PLACEMENT, FELLOWSHIP

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(columnDefinition = "TEXT")
    public String eligibilitySummary;

    @Column(length = 140)
    public String requiredQualification;

    @Column(length = 140)
    public String requiredSpecialization;

    @Column(columnDefinition = "TEXT")
    public String requiredSkillsJson; // list of skill names

    @Column(length = 120)
    public String location;

    @Column(length = 30)
    public String workMode = "ON_SITE"; // ON_SITE, HYBRID, REMOTE

    @Column(length = 80)
    public String duration;

    @Column(length = 100)
    public String stipendOrSalary;

    @Column(length = 40)
    public String deadline;

    @Column(length = 500)
    public String officialUrl;

    @Column(length = 64)
    public String publishedByUserId;

    @Column(length = 40)
    public String verificationStatus = "VERIFIED"; // VERIFIED, ORGANIZATION_POSTED, EXTERNAL

    public LocalDateTime createdAt = LocalDateTime.now();

    public Opportunity() {}

    public Opportunity(String id, String title, String organization, String type, String eligibilitySummary, String requiredQualification, String requiredSpecialization, String requiredSkillsJson, String location, String workMode, String duration, String stipendOrSalary, String deadline, String officialUrl, String verificationStatus) {
        this.id = id;
        this.title = title;
        this.organization = organization;
        this.type = type;
        this.eligibilitySummary = eligibilitySummary;
        this.requiredQualification = requiredQualification;
        this.requiredSpecialization = requiredSpecialization;
        this.requiredSkillsJson = requiredSkillsJson;
        this.location = location;
        this.workMode = workMode;
        this.duration = duration;
        this.stipendOrSalary = stipendOrSalary;
        this.deadline = deadline;
        this.officialUrl = officialUrl;
        this.verificationStatus = verificationStatus;
        this.createdAt = LocalDateTime.now();
    }
}
