package com.healthskillconnect.model;

import jakarta.persistence.*;

@Entity
@Table(name = "career_paths")
public class CareerPath {

    @Id
    @Column(nullable = false, length = 64)
    public String id;

    @Column(nullable = false, unique = true, length = 140)
    public String name;

    @Column(nullable = false, length = 80)
    public String category; // Clinical Research, Health Informatics, Laboratory & Diagnostics, Public Health, Healthcare Administration, Clinical Practice

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(columnDefinition = "TEXT")
    public String requiredQualifications; // e.g. "BSc Nursing, MBBS, BPharm, Life Sciences, BPT"

    @Column(columnDefinition = "TEXT")
    public String requiredSpecializations;

    @Column(columnDefinition = "TEXT")
    public String requiredSkills; // comma-delimited or JSON list

    @Column(columnDefinition = "TEXT")
    public String recommendedCertifications;

    @Column(length = 255)
    public String experienceSummary;

    @Column(length = 100)
    public String averageSalaryRange;

    @Column(length = 100)
    public String marketOutlook; // High Growth, Very High Demand, Stable

    public CareerPath() {}

    public CareerPath(String id, String name, String category, String description, String requiredQualifications, String requiredSpecializations, String requiredSkills, String recommendedCertifications, String averageSalaryRange, String marketOutlook) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.requiredQualifications = requiredQualifications;
        this.requiredSpecializations = requiredSpecializations;
        this.requiredSkills = requiredSkills;
        this.recommendedCertifications = recommendedCertifications;
        this.averageSalaryRange = averageSalaryRange;
        this.marketOutlook = marketOutlook;
    }
}
