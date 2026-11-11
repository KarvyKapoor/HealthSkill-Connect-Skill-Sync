package com.healthskillconnect.model;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @Column(nullable = false, length = 64)
    public String id;

    @Column(nullable = false, unique = true, length = 120)
    public String name;

    @Column(nullable = false, length = 80)
    public String category; // Clinical Knowledge, Diagnostics & Lab, Health Informatics & Tech, Research & Epidemiology, Healthcare Administration, Patient Care & Safety

    @Column(length = 500)
    public String description;

    @Column(columnDefinition = "TEXT")
    public String relatedSpecializations;

    @Column(columnDefinition = "TEXT")
    public String relatedCareers;

    public boolean inDemand = true;

    public Skill() {}

    public Skill(String id, String name, String category, String description, String relatedSpecializations, String relatedCareers) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.relatedSpecializations = relatedSpecializations;
        this.relatedCareers = relatedCareers;
        this.inDemand = true;
    }
}
