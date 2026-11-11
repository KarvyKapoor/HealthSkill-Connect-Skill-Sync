package com.healthskillconnect.repository;

import com.healthskillconnect.model.MentorshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    List<MentorshipRequest> findByMentorIdOrderByCreatedAtDesc(String mentorId);
    List<MentorshipRequest> findByStudentIdOrderByCreatedAtDesc(String studentId);
}
