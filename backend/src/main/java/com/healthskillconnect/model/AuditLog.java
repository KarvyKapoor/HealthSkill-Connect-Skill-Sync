package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actorUserId"),
    @Index(name = "idx_audit_event", columnList = "eventType"),
    @Index(name = "idx_audit_created", columnList = "createdAt")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 64)
    public String actorUserId;

    @Column(length = 180)
    public String actorEmail;

    @Column(length = 40)
    public String actorRole;

    @Column(nullable = false, length = 60)
    public String eventType;

    @Column(length = 60)
    public String targetEntityType;

    @Column(length = 120)
    public String targetEntityId;

    @Column(nullable = false, length = 30)
    public String resultStatus = "SUCCESS"; // SUCCESS, FAILURE

    @Column(length = 60)
    public String ipAddress;

    @Column(columnDefinition = "TEXT")
    public String detailsJson;

    public LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String actorUserId, String actorEmail, String actorRole, String eventType, String targetEntityType, String targetEntityId, String resultStatus, String ipAddress, String detailsJson) {
        this.actorUserId = actorUserId;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.eventType = eventType;
        this.targetEntityType = targetEntityType;
        this.targetEntityId = targetEntityId;
        this.resultStatus = resultStatus != null ? resultStatus : "SUCCESS";
        this.ipAddress = ipAddress;
        this.detailsJson = detailsJson;
        this.createdAt = LocalDateTime.now();
    }
}
