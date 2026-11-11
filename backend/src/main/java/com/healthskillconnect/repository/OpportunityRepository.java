package com.healthskillconnect.repository;

import com.healthskillconnect.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, String> {
    List<Opportunity> findAllByOrderByCreatedAtDesc();
    List<Opportunity> findByType(String type);
    List<Opportunity> findByPublishedByUserIdOrderByCreatedAtDesc(String userId);
}
