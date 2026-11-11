package com.healthskillconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments", indexes = {
    @Index(name = "idx_comment_post", columnList = "postId")
})
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long postId;

    @Column(nullable = false, length = 64)
    public String authorId;

    @Column(nullable = false, length = 120)
    public String authorName;

    @Column(nullable = false, length = 40)
    public String authorRole;

    @Column(length = 10)
    public String authorAvatar;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    public LocalDateTime createdAt = LocalDateTime.now();

    public PostComment() {}

    public PostComment(Long postId, String authorId, String authorName, String authorRole, String authorAvatar, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.authorAvatar = authorAvatar;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
