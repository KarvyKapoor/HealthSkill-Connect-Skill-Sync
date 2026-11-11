package com.healthskillconnect.repository;

import com.healthskillconnect.model.OpportunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, Long> {
    List<OpportunityApplication> findByOpportunityIdOrderByAppliedAtDesc(String opportunityId);
    List<OpportunityApplication> findByUserIdOrderByAppliedAtDesc(String userId);
    Optional<OpportunityApplication> findByOpportunityIdAndUserId(String opportunityId, String userId);
}
