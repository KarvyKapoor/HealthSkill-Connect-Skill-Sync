package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_plans", indexes = {
    @Index(name = "idx_study_plan_user", columnList = "userId")
})
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(nullable = false, length = 180)
    public String title;

    @Column(length = 140)
    public String targetCareer;

    public int hoursPerWeek = 10;

    @Column(columnDefinition = "MEDIUMTEXT")
    public String scheduleJson; // Daily/weekly time allocations and topics

    public LocalDateTime createdAt = LocalDateTime.now();

    public StudyPlan() {}

    public StudyPlan(String userId, String title, String targetCareer, int hoursPerWeek, String scheduleJson) {
        this.userId = userId;
        this.title = title;
        this.targetCareer = targetCareer;
        this.hoursPerWeek = hoursPerWeek;
        this.scheduleJson = scheduleJson;
        this.createdAt = LocalDateTime.now();
    }
}
