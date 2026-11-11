package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "connections", indexes = {
    @Index(name = "idx_conn_req_rec", columnList = "requesterId, recipientId")
})
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String requesterId;

    @Column(nullable = false, length = 64)
    public String recipientId;

    @Column(nullable = false, length = 30)
    public String status = "PENDING"; // PENDING, ACCEPTED, DECLINED

    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();

    public Connection() {}

    public Connection(String requesterId, String recipientId, String status) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
