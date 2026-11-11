package com.healthskillconnect.repository;

import com.healthskillconnect.model.LearningRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {
    List<LearningRoadmap> findByUserIdOrderByCreatedAtDesc(String userId);
}
