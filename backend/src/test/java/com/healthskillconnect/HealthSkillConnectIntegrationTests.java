package com.healthskillconnect;

import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import com.healthskillconnect.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HealthSkillConnectIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private SkillCareerService skillCareerService;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private NetworkingService networkingService;

    @Autowired
    private LearningContentService learningContentService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @Transactional
    void testUserRegistrationAndRoleIntegrity() {
        String testEmail = "test.nurse." + System.currentTimeMillis() + "@example.com";
        AppUser user = userService.register(
                "Priya Sharma",
                testEmail,
                "StrongPass123!",
                "Nursing",
                "Year 2",
                "Bengaluru"
        );

        assertNotNull(user.id);
        assertEquals("STUDENT", user.role);
        assertEquals("PS", user.avatarInitials);
        assertTrue(userService.matches(user, "StrongPass123!"));
        assertFalse(userService.matches(user, "WrongPass123!"));
    }

    @Test
    @Transactional
    void testCareerRecommendationsAndExplainability() {
        AppUser demoStudent = userService.byEmail("aarav.student@example.com");
        assertNotNull(demoStudent);

        List<Map<String, Object>> recs = skillCareerService.calculateCareerRecommendations(demoStudent);
        assertNotNull(recs);
        assertFalse(recs.isEmpty());

        Map<String, Object> topRec = recs.get(0);
        assertTrue(topRec.containsKey("careerName"));
        assertTrue(topRec.containsKey("matchPercentage"));
        assertTrue(topRec.containsKey("explanation"));
        assertTrue((int) topRec.get("matchPercentage") > 0);
    }

    @Test
    @Transactional
    void testMarketReadinessCalculation() {
        AppUser demoStudent = userService.byEmail("meera.student@example.com");
        assertNotNull(demoStudent);

        Map<String, Object> readiness = skillCareerService.calculateMarketReadiness(demoStudent);
        assertNotNull(readiness);
        assertTrue((int) readiness.get("readinessScore") >= 0);
        assertNotNull(readiness.get("readinessTier"));
        assertNotNull(readiness.get("inDemandSkills"));
    }

    @Test
    @Transactional
    void testOpportunityApplicationAndSaveWorkflow() {
        AppUser demoStudent = userService.byEmail("aarav.student@example.com");
        assertNotNull(demoStudent);

        List<Map<String, Object>> opps = opportunityService.getOpportunitiesWithMatch(demoStudent, "ALL");
        assertFalse(opps.isEmpty());

        String oppId = (String) opps.get(0).get("id");

        // Toggle save
        boolean saved = opportunityService.toggleSave(demoStudent.id, oppId);
        assertTrue(saved);

        // Apply
        OpportunityApplication app = opportunityService.apply(
                demoStudent.id,
                oppId,
                demoStudent.fullName,
                demoStudent.email,
                demoStudent.specialization,
                "Passionate about patient safety research."
        );
        assertNotNull(app.id);
        assertEquals("APPLIED", app.status);

        List<OpportunityApplication> myApps = opportunityService.getApplicationsForUser(demoStudent.id);
        assertTrue(myApps.stream().anyMatch(a -> a.opportunityId.equals(oppId)));
    }

    @Test
    @Transactional
    void testNetworkingFeedAndInteractions() {
        AppUser mentor = userService.byEmail("nisha.mentor@example.com");
        AppUser student = userService.byEmail("aarav.student@example.com");
        assertNotNull(mentor);
        assertNotNull(student);

        Post post = networkingService.createPost(
                mentor,
                "Study Design Tips for Nursing Researchers",
                "Always verify whether the cohort was prospective or retrospective before generalizing results.",
                "RESEARCH_UPDATE",
                List.of("Nursing", "Research")
        );
        assertNotNull(post.id);

        // React
        Map<String, Object> reaction = networkingService.toggleReaction(post.id, student.id);
        assertEquals(true, reaction.get("liked"));
        assertEquals(1, reaction.get("likesCount"));

        // Comment
        PostComment comment = networkingService.addComment(post.id, student, "Thank you Dr. Nisha, very helpful!");
        assertNotNull(comment.id);
        assertEquals("A", comment.authorAvatar != null ? comment.authorAvatar.substring(0, 1) : "A");
    }

    @Test
    @Transactional
    void testManualNoteCreationAndPersistence() {
        AppUser student = userService.byEmail("aarav.student@example.com");
        assertNotNull(student);

        Note note = learningContentService.saveNote(
                student.id,
                "Surgical Safety Checklist Protocol",
                "Patient safety",
                "Clinical practice",
                "Beginner",
                "Sign-in, Time-out, Sign-out phases.",
                List.of(Map.of("heading", "Time-Out", "content", "Full team pause before incision.")),
                List.of(Map.of("question", "Who initiates the time-out?", "answer", "Any designated surgical team member.")),
                List.of("Surgery", "Checklist"),
                false,
                "PRIVATE"
        );

        assertNotNull(note.id);
        assertFalse(note.isAiGenerated);

        List<Note> userNotes = learningContentService.getUserNotes(student.id);
        assertTrue(userNotes.stream().anyMatch(n -> n.id.equals(note.id)));
    }
}
