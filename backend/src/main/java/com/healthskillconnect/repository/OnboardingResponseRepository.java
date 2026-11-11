package com.healthskillconnect.repository;

import com.healthskillconnect.model.OnboardingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingResponseRepository extends JpaRepository<OnboardingResponse, Long> {
    Optional<OnboardingResponse> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
