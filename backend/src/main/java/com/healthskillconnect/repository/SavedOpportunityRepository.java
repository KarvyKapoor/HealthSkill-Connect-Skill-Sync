package com.healthskillconnect.repository;

import com.healthskillconnect.model.SavedOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedOpportunityRepository extends JpaRepository<SavedOpportunity, Long> {
    List<SavedOpportunity> findByUserIdOrderBySavedAtDesc(String userId);
    Optional<SavedOpportunity> findByUserIdAndOpportunityId(String userId, String opportunityId);
    void deleteByUserIdAndOpportunityId(String userId, String opportunityId);
}
