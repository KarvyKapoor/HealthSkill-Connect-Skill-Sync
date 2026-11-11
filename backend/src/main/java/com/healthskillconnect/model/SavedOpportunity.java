package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_opportunities", indexes = {
    @Index(name = "idx_saved_opp_user", columnList = "userId, opportunityId", unique = true)
})
public class SavedOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userId;

    @Column(nullable = false, length = 64)
    public String opportunityId;

    public LocalDateTime savedAt = LocalDateTime.now();

    public SavedOpportunity() {}

    public SavedOpportunity(String userId, String opportunityId) {
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.savedAt = LocalDateTime.now();
    }
}
