package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_roadmaps", indexes = {
    @Index(name = "idx_roadmaps_user", columnList = "userId")
})
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(nullable = false, length = 255)
    public String goal;

    @Column(length = 120)
    public String specialization;

    public int durationWeeks = 4;

    @Column(columnDefinition = "MEDIUMTEXT")
    public String contentJson; // JSON representation of weekly modules/activities

    public boolean isAiGenerated = true;

    public LocalDateTime createdAt = LocalDateTime.now();

    public LearningRoadmap() {}

    public LearningRoadmap(String userId, String goal, String specialization, int durationWeeks, String contentJson, boolean isAiGenerated) {
        this.userId = userId;
        this.goal = goal;
        this.specialization = specialization;
        this.durationWeeks = durationWeeks;
        this.contentJson = contentJson;
        this.isAiGenerated = isAiGenerated;
        this.createdAt = LocalDateTime.now();
    }
}
