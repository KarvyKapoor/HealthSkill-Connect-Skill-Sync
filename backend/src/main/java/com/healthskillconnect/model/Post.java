package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String authorId;

    @Column(nullable = false, length = 120)
    public String authorName;

    @Column(nullable = false, length = 40)
    public String authorRole;

    @Column(length = 120)
    public String authorSpecialization;

    @Column(length = 10)
    public String authorAvatar;

    @Column(length = 200)
    public String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(nullable = false, length = 60)
    public String category = "CLINICAL_INSIGHT"; // CLINICAL_INSIGHT, CASE_STUDY, RESEARCH_UPDATE, CAREER_ADVICE, LEARNING_RESOURCE, GENERAL_DISCUSSION

    @Column(columnDefinition = "TEXT")
    public String tagsJson;

    public int likesCount = 0;
    public int commentsCount = 0;

    public boolean verifiedHealthcareOnly = false;

    public LocalDateTime createdAt = LocalDateTime.now();

    public Post() {}

    public Post(String authorId, String authorName, String authorRole, String authorSpecialization, String authorAvatar, String title, String content, String category, String tagsJson) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.authorSpecialization = authorSpecialization;
        this.authorAvatar = authorAvatar;
        this.title = title;
        this.content = content;
        this.category = category != null ? category : "CLINICAL_INSIGHT";
        this.tagsJson = tagsJson;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}
