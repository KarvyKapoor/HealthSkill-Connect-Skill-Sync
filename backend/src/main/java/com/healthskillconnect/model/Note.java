package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes", indexes = {
    @Index(name = "idx_notes_user", columnList = "userId")
})
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(nullable = false, length = 200)
    public String title;

    @Column(nullable = false, length = 140)
    public String topic;

    @Column(length = 80)
    public String category = "General Healthcare";

    @Column(length = 40)
    public String level = "Beginner";

    @Column(columnDefinition = "MEDIUMTEXT")
    public String content;

    @Column(columnDefinition = "MEDIUMTEXT")
    public String sectionsJson; // Array of section objects {heading, content, items}

    @Column(columnDefinition = "TEXT")
    public String selfTestJson; // Array of {question, answer}

    @Column(columnDefinition = "TEXT")
    public String tagsJson;

    public boolean isAiGenerated = false;

    @Column(length = 30)
    public String visibility = "PRIVATE"; // PRIVATE, SHARED, PUBLIC

    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();

    public Note() {}

    public Note(String userId, String title, String topic, String category, String level, String content, String sectionsJson, String selfTestJson, String tagsJson, boolean isAiGenerated, String visibility) {
        this.userId = userId;
        this.title = title;
        this.topic = topic;
        this.category = category != null ? category : "General Healthcare";
        this.level = level != null ? level : "Beginner";
        this.content = content;
        this.sectionsJson = sectionsJson;
        this.selfTestJson = selfTestJson;
        this.tagsJson = tagsJson;
        this.isAiGenerated = isAiGenerated;
        this.visibility = visibility != null ? visibility : "PRIVATE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
