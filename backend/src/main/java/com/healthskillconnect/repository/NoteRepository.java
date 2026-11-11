package com.healthskillconnect.repository;

import com.healthskillconnect.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByUpdatedAtDesc(String userId);
    Optional<Note> findByIdAndUserId(Long id, String userId);
    List<Note> findByVisibilityOrderByUpdatedAtDesc(String visibility);
}
