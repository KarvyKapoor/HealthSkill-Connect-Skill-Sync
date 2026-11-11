package com.healthskillconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LearningContentService {

    private final NoteRepository noteRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ObjectMapper objectMapper;

    public LearningContentService(
            NoteRepository noteRepository,
            LearningRoadmapRepository roadmapRepository,
            StudyPlanRepository studyPlanRepository,
            ObjectMapper objectMapper) {
        this.noteRepository = noteRepository;
        this.roadmapRepository = roadmapRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.objectMapper = objectMapper;
    }

    public List<Note> getUserNotes(String userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Optional<Note> getNoteById(Long id, String userId) {
        return noteRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Note saveNote(String userId, String title, String topic, String category, String level, String content, Object sections, Object selfTest, List<String> tags, boolean isAiGenerated, String visibility) {
        String sectionsJson = "";
        String selfTestJson = "";
        String tagsJson = "";
        try {
            if (sections != null) sectionsJson = objectMapper.writeValueAsString(sections);
            if (selfTest != null) selfTestJson = objectMapper.writeValueAsString(selfTest);
            if (tags != null) tagsJson = objectMapper.writeValueAsString(tags);
        } catch (Exception ignored) {}

        Note note = new Note(
                userId,
                title != null && !title.isBlank() ? title.trim() : topic + " Notes",
                topic != null ? topic.trim() : "General",
                category != null ? category.trim() : "Healthcare Education",
                level != null ? level.trim() : "Beginner",
                content != null ? content.trim() : "",
                sectionsJson,
                selfTestJson,
                tagsJson,
                isAiGenerated,
                visibility != null ? visibility.trim() : "PRIVATE"
        );
        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, String userId, Map<String, Object> updates) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Note not found."));

        if (updates.containsKey("title") && updates.get("title") instanceof String s) {
            note.title = s.trim();
        }
        if (updates.containsKey("content") && updates.get("content") instanceof String s) {
            note.content = s.trim();
        }
        if (updates.containsKey("category") && updates.get("category") instanceof String s) {
            note.category = s.trim();
        }
        if (updates.containsKey("level") && updates.get("level") instanceof String s) {
            note.level = s.trim();
        }
        if (updates.containsKey("visibility") && updates.get("visibility") instanceof String s) {
            note.visibility = s.trim();
        }
        if (updates.containsKey("sections")) {
            try {
                note.sectionsJson = objectMapper.writeValueAsString(updates.get("sections"));
            } catch (Exception ignored) {}
        }
        if (updates.containsKey("selfTest")) {
            try {
                note.selfTestJson = objectMapper.writeValueAsString(updates.get("selfTest"));
            } catch (Exception ignored) {}
        }

        note.updatedAt = LocalDateTime.now();
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(Long id, String userId) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Note not found."));
        noteRepository.delete(note);
    }

    @Transactional
    public LearningRoadmap saveRoadmap(String userId, String goal, String specialization, int durationWeeks, Object content, boolean isAiGenerated) {
        String contentJson = "";
        try {
            contentJson = objectMapper.writeValueAsString(content);
        } catch (Exception ignored) {}

        LearningRoadmap r = new LearningRoadmap(userId, goal, specialization, durationWeeks, contentJson, isAiGenerated);
        return roadmapRepository.save(r);
    }

    public List<LearningRoadmap> getUserRoadmaps(String userId) {
        return roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public StudyPlan saveStudyPlan(String userId, String title, String targetCareer, int hoursPerWeek, Object schedule) {
        String scheduleJson = "";
        try {
            scheduleJson = objectMapper.writeValueAsString(schedule);
        } catch (Exception ignored) {}

        StudyPlan plan = new StudyPlan(userId, title, targetCareer, hoursPerWeek, scheduleJson);
        return studyPlanRepository.save(plan);
    }

    public List<StudyPlan> getUserStudyPlans(String userId) {
        return studyPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
