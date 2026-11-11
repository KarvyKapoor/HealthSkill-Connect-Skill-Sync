package com.healthskillconnect.repository;

import com.healthskillconnect.model.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    Optional<PostReaction> findByPostIdAndUserId(Long postId, String userId);
    List<PostReaction> findByPostId(Long postId);
    long countByPostId(Long postId);
    void deleteByPostIdAndUserId(Long postId, String userId);
}
