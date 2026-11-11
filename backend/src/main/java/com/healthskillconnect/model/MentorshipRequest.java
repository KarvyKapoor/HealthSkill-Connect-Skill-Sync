package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentorship_requests", indexes = {
    @Index(name = "idx_mentor_req_mentor", columnList = "mentorId"),
    @Index(name = "idx_mentor_req_student", columnList = "studentId")
})
public class MentorshipRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String studentId;

    @Column(nullable = false, length = 120)
    public String studentName;

    @Column(length = 120)
    public String studentSpecialization;

    @Column(length = 10)
    public String studentAvatar;

    @Column(nullable = false, length = 64)
    public String mentorId;

    @Column(nullable = false, length = 180)
    public String subject;

    @Column(columnDefinition = "TEXT")
    public String goals;

    @Column(columnDefinition = "TEXT")
    public String message;

    @Column(nullable = false, length = 30)
    public String status = "PENDING"; // PENDING, ACCEPTED, DECLINED, COMPLETED

    public LocalDateTime scheduledAt;

    @Column(columnDefinition = "TEXT")
    public String notes;

    public LocalDateTime createdAt = LocalDateTime.now();

    public MentorshipRequest() {}

    public MentorshipRequest(String studentId, String studentName, String studentSpecialization, String studentAvatar, String mentorId, String subject, String goals, String message) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentSpecialization = studentSpecialization;
        this.studentAvatar = studentAvatar;
        this.mentorId = mentorId;
        this.subject = subject;
        this.goals = goals;
        this.message = message;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
}
