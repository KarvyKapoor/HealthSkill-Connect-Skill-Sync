package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills", indexes = {
    @Index(name = "idx_user_skill_user", columnList = "userId"),
    @Index(name = "idx_user_skill_name", columnList = "skillName")
})
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(length = 64)
    public String skillId;

    @Column(nullable = false, length = 120)
    public String skillName;

    @Column(nullable = false, length = 40)
    public String proficiency = "Beginner"; // Beginner, Basic, Intermediate, Advanced, Expert

    @Column(length = 40)
    public String evidenceType = "SELF_REPORTED"; // SELF_REPORTED, CERTIFICATE, PROJECT, WORK_EXPERIENCE, ASSESSMENT

    @Column(length = 255)
    public String evidenceTitle;

    public boolean verified = false;

    public LocalDateTime createdAt = LocalDateTime.now();

    public UserSkill() {}

    public UserSkill(String userId, String skillId, String skillName, String proficiency, String evidenceType, String evidenceTitle, boolean verified) {
        this.userId = userId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiency = proficiency;
        this.evidenceType = evidenceType != null ? evidenceType : "SELF_REPORTED";
        this.evidenceTitle = evidenceTitle;
        this.verified = verified;
        this.createdAt = LocalDateTime.now();
    }
}
