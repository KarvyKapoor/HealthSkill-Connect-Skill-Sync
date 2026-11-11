package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_reactions", indexes = {
    @Index(name = "idx_reaction_post_user", columnList = "postId, userId", unique = true)
})
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long postId;

    @Column(nullable = false, length = 64)
    public String userId;

    public LocalDateTime createdAt = LocalDateTime.now();

    public PostReaction() {}

    public PostReaction(Long postId, String userId) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
