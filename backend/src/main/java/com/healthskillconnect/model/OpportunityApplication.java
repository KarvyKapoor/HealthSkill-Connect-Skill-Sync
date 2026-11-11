package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunity_applications", indexes = {
    @Index(name = "idx_app_opp_user", columnList = "opportunityId, userId")
})
public class OpportunityApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String opportunityId;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(nullable = false, length = 120)
    public String applicantName;

    @Column(nullable = false, length = 180)
    public String applicantEmail;

    @Column(length = 120)
    public String applicantSpecialization;

    @Column(columnDefinition = "TEXT")
    public String coverNote;

    @Column(nullable = false, length = 40)
    public String status = "APPLIED"; // APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED, ACCEPTED

    public LocalDateTime appliedAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();

    public OpportunityApplication() {}

    public OpportunityApplication(String opportunityId, String userId, String applicantName, String applicantEmail, String applicantSpecialization, String coverNote) {
        this.opportunityId = opportunityId;
        this.userId = userId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantSpecialization = applicantSpecialization;
        this.coverNote = coverNote;
        this.status = "APPLIED";
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
