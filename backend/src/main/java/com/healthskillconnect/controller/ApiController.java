package com.healthskillconnect.controller;

import com.healthskillconnect.data.JsonDataLoader;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.AuditLogRepository;
import com.healthskillconnect.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final AiClient ai;
    private final JsonDataLoader data;
    private final UserService users;
    private final SkillCareerService skillCareerService;
    private final OpportunityService opportunityService;
    private final NetworkingService networkingService;
    private final LearningContentService learningContentService;
    private final OnboardingService onboardingService;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;

    public ApiController(
            AiClient ai,
            JsonDataLoader data,
            UserService users,
            SkillCareerService skillCareerService,
            OpportunityService opportunityService,
            NetworkingService networkingService,
            LearningContentService learningContentService,
            OnboardingService onboardingService,
            AuditService auditService,
            AuditLogRepository auditLogRepository) {
        this.ai = ai;
        this.data = data;
        this.users = users;
        this.skillCareerService = skillCareerService;
        this.opportunityService = opportunityService;
        this.networkingService = networkingService;
        this.learningContentService = learningContentService;
        this.onboardingService = onboardingService;
        this.auditService = auditService;
        this.auditLogRepository = auditLogRepository;
    }

    // Health
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "java-backend", "database", "mysql-active");
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(HttpServletRequest request) {
        return authenticatedUser(request).publicMap();
    }

    // Profile Management
    @PatchMapping("/users/profile")
    public Map<String, Object> updateProfile(
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        AppUser updated = users.updateProfile(user.id, updates);

        auditService.logEvent(
                user.id, user.email, user.role,
                "PROFILE_UPDATE", "USER", user.id, "SUCCESS", request,
                Map.of("fieldsUpdated", updates.keySet())
        );

        return updated.publicMap();
    }

    // Student Onboarding
    @PostMapping("/onboarding/submit")
    public Map<String, Object> submitOnboarding(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        Map<String, Object> result = onboardingService.submitQuestionnaire(user, body);

        auditService.logEvent(
                user.id, user.email, user.role,
                "QUESTIONNAIRE_SUBMIT", "ONBOARDING", user.id, "SUCCESS", request,
                Map.of("qualification", body.getOrDefault("currentQualification", "N/A"))
        );

        return result;
    }

    @GetMapping("/onboarding/latest")
    public Map<String, Object> getLatestOnboarding(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return onboardingService.getLatestResponse(user.id)
                .map(r -> Map.<String, Object>of("response", r, "exists", true))
                .orElse(Map.of("exists", false));
    }

    // Skills Taxonomy & User Skills
    @GetMapping("/skills")
    public List<Skill> skills() {
        return skillCareerService.getAllSkills();
    }

    @GetMapping("/users/skills")
    public List<UserSkill> userSkills(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return skillCareerService.getUserSkills(user.id);
    }

    @PostMapping("/users/skills")
    public UserSkill addUserSkill(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name is required.");
        }
        String prof = body.getOrDefault("proficiency", "Intermediate");
        String evType = body.getOrDefault("evidenceType", "SELF_REPORTED");
        String evTitle = body.get("evidenceTitle");

        UserSkill saved = skillCareerService.addUserSkill(user.id, name, prof, evType, evTitle);

        auditService.logEvent(
                user.id, user.email, user.role,
                "SKILL_ADD", "SKILL", saved.id.toString(), "SUCCESS", request,
                Map.of("skillName", name, "proficiency", prof)
        );

        return saved;
    }

    @DeleteMapping("/users/skills/{id}")
    public Map<String, Object> deleteUserSkill(@PathVariable Long id, HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        skillCareerService.deleteUserSkill(id);
        return Map.of("success", true, "deletedId", id);
    }

    // Career Paths & Recommendations
    @GetMapping("/careers")
    public List<CareerPath> careers() {
        return skillCareerService.getAllCareerPaths();
    }

    @GetMapping("/careers/recommendations")
    public List<Map<String, Object>> careerRecommendations(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return skillCareerService.calculateCareerRecommendations(user);
    }

    @GetMapping("/market-readiness")
    public Map<String, Object> marketReadiness(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return skillCareerService.calculateMarketReadiness(user);
    }

    // Jobs & Opportunities
    @GetMapping("/opportunities")
    public List<Map<String, Object>> opportunities(
            @RequestParam(name = "type", required = false) String type,
            HttpServletRequest request
    ) {
        AppUser user = optionalAuthenticatedUser(request);
        return opportunityService.getOpportunitiesWithMatch(user, type);
    }

    @PostMapping("/opportunities")
    public Opportunity createOpportunity(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String title = (String) body.get("title");
        String org = (String) body.get("organization");
        String type = (String) body.get("type");
        String desc = (String) body.get("description");
        String elig = (String) body.get("eligibilitySummary");
        String qual = (String) body.get("requiredQualification");
        String spec = (String) body.get("requiredSpecialization");
        List<String> skills = (List<String>) body.get("requiredSkills");
        String loc = (String) body.get("location");
        String mode = (String) body.get("workMode");
        String duration = (String) body.get("duration");
        String stipend = (String) body.get("stipendOrSalary");
        String deadline = (String) body.get("deadline");
        String url = (String) body.get("officialUrl");

        if (title == null || title.isBlank() || org == null || org.isBlank()) {
            throw new IllegalArgumentException("Title and Organization are required.");
        }

        Opportunity opp = opportunityService.createOpportunity(
                title, org, type, desc, elig, qual, spec, skills, loc, mode, duration, stipend, deadline, url, user.id
        );

        auditService.logEvent(
                user.id, user.email, user.role,
                "OPPORTUNITY_POST", "OPPORTUNITY", opp.id, "SUCCESS", request,
                Map.of("title", title, "organization", org)
        );

        return opp;
    }

    @PostMapping("/opportunities/{id}/save")
    public Map<String, Object> toggleSaveOpportunity(@PathVariable String id, HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        boolean saved = opportunityService.toggleSave(user.id, id);

        auditService.logEvent(
                user.id, user.email, user.role,
                saved ? "OPPORTUNITY_SAVE" : "OPPORTUNITY_UNSAVE",
                "OPPORTUNITY", id, "SUCCESS", request, null
        );

        return Map.of("saved", saved, "opportunityId", id);
    }

    @PostMapping("/opportunities/{id}/apply")
    public OpportunityApplication applyOpportunity(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String coverNote = body.getOrDefault("coverNote", "");
        String applicantName = body.getOrDefault("applicantName", user.fullName);
        String applicantEmail = body.getOrDefault("applicantEmail", user.email);
        String applicantSpec = body.getOrDefault("applicantSpecialization", user.specialization);

        OpportunityApplication app = opportunityService.apply(
                user.id, id, applicantName, applicantEmail, applicantSpec, coverNote
        );

        auditService.logEvent(
                user.id, user.email, user.role,
                "OPPORTUNITY_APPLY", "OPPORTUNITY", id, "SUCCESS", request,
                Map.of("applicationId", app.id)
        );

        return app;
    }

    @GetMapping("/opportunities/my-applications")
    public List<OpportunityApplication> myApplications(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return opportunityService.getApplicationsForUser(user.id);
    }

    @GetMapping("/opportunities/{id}/applications")
    public List<OpportunityApplication> opportunityApplications(
            @PathVariable String id,
            HttpServletRequest request
    ) {
        authenticatedUser(request);
        return opportunityService.getApplicationsForOpportunity(id);
    }

    // Networking
    @GetMapping("/network/feed")
    public List<Map<String, Object>> networkFeed(
            @RequestParam(name = "category", required = false) String category,
            HttpServletRequest request
    ) {
        AppUser user = optionalAuthenticatedUser(request);
        return networkingService.getFeed(user != null ? user.id : null, category);
    }

    @PostMapping("/network/posts")
    public Post createPost(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String category = (String) body.get("category");
        List<String> tags = (List<String>) body.get("tags");

        Post p = networkingService.createPost(user, title, content, category, tags);

        auditService.logEvent(
                user.id, user.email, user.role,
                "POST_CREATE", "POST", p.id.toString(), "SUCCESS", request,
                Map.of("category", p.category)
        );

        return p;
    }

    @PostMapping("/network/posts/{id}/react")
    public Map<String, Object> reactPost(@PathVariable Long id, HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return networkingService.toggleReaction(id, user.id);
    }

    @PostMapping("/network/posts/{id}/comments")
    public PostComment addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String content = body.get("content");
        PostComment comment = networkingService.addComment(id, user, content);

        auditService.logEvent(
                user.id, user.email, user.role,
                "COMMENT_CREATE", "POST", id.toString(), "SUCCESS", request,
                Map.of("commentId", comment.id)
        );

        return comment;
    }

    @GetMapping("/network/connections")
    public List<Map<String, Object>> connections(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return networkingService.getConnectionsForUser(user.id);
    }

    @PostMapping("/network/connect/{recipientId}")
    public Connection connect(
            @PathVariable String recipientId,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        Connection c = networkingService.sendConnectionRequest(user.id, recipientId);

        auditService.logEvent(
                user.id, user.email, user.role,
                "CONNECTION_REQUEST", "USER", recipientId, "SUCCESS", request,
                Map.of("connectionId", c.id)
        );

        return c;
    }

    @PatchMapping("/network/connections/{id}")
    public Connection updateConnection(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String action = body.getOrDefault("action", "ACCEPT");
        Connection c = networkingService.updateConnectionStatus(id, user.id, action);

        auditService.logEvent(
                user.id, user.email, user.role,
                "CONNECTION_" + action.toUpperCase(), "CONNECTION", id.toString(), "SUCCESS", request, null
        );

        return c;
    }

    @GetMapping("/network/members")
    public List<Map<String, Object>> members(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "role", required = false) String role,
            HttpServletRequest request
    ) {
        AppUser user = optionalAuthenticatedUser(request);
        return networkingService.getDiscoverableMembers(user != null ? user.id : null, query, role);
    }

    @PostMapping("/network/mentorship-request")
    public MentorshipRequest requestMentorship(
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String mentorId = body.get("mentorId");
        String subject = body.get("subject");
        String goals = body.get("goals");
        String message = body.get("message");

        if (mentorId == null || mentorId.isBlank()) {
            throw new IllegalArgumentException("Mentor ID is required.");
        }

        MentorshipRequest mr = networkingService.requestMentorship(user, mentorId, subject, goals, message);

        auditService.logEvent(
                user.id, user.email, user.role,
                "MENTORSHIP_REQUEST", "USER", mentorId, "SUCCESS", request,
                Map.of("requestId", mr.id)
        );

        return mr;
    }

    @GetMapping("/network/mentorship-requests")
    public List<MentorshipRequest> mentorshipRequests(
            @RequestParam(name = "asMentor", defaultValue = "false") boolean asMentor,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        return networkingService.getMentorshipRequestsForUser(user.id, asMentor);
    }

    @PatchMapping("/network/mentorship-requests/{id}")
    public MentorshipRequest updateMentorshipRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String status = body.getOrDefault("status", "ACCEPTED");
        String notes = body.get("notes");
        return networkingService.updateMentorshipStatus(id, user.id, status, notes);
    }

    // AI Studio & Content Library
    @GetMapping("/content/notes")
    public List<Note> getNotes(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return learningContentService.getUserNotes(user.id);
    }

    @PostMapping("/content/notes")
    public Note createNote(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String title = (String) body.get("title");
        String topic = (String) body.get("topic");
        String category = (String) body.get("category");
        String level = (String) body.get("level");
        String content = (String) body.get("content");
        Object sections = body.get("sections");
        Object selfTest = body.get("selfTest");
        List<String> tags = (List<String>) body.get("tags");
        boolean isAi = Boolean.TRUE.equals(body.get("isAiGenerated"));
        String visibility = (String) body.get("visibility");

        Note note = learningContentService.saveNote(
                user.id, title, topic, category, level, content, sections, selfTest, tags, isAi, visibility
        );

        auditService.logEvent(
                user.id, user.email, user.role,
                "NOTE_CREATE", "NOTE", note.id.toString(), "SUCCESS", request,
                Map.of("isAiGenerated", isAi, "topic", note.topic)
        );

        return note;
    }

    @PatchMapping("/content/notes/{id}")
    public Note updateNote(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        Note note = learningContentService.updateNote(id, user.id, body);

        auditService.logEvent(
                user.id, user.email, user.role,
                "NOTE_UPDATE", "NOTE", id.toString(), "SUCCESS", request, null
        );

        return note;
    }

    @DeleteMapping("/content/notes/{id}")
    public Map<String, Object> deleteNote(@PathVariable Long id, HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        learningContentService.deleteNote(id, user.id);

        auditService.logEvent(
                user.id, user.email, user.role,
                "NOTE_DELETE", "NOTE", id.toString(), "SUCCESS", request, null
        );

        return Map.of("success", true, "deletedId", id);
    }

    @PostMapping("/ai/save-roadmap")
    public LearningRoadmap saveRoadmap(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String goal = (String) body.get("goal");
        String spec = (String) body.getOrDefault("specialization", user.specialization);
        int weeks = body.get("durationWeeks") instanceof Number n ? n.intValue() : 4;
        Object content = body.get("weeks");

        LearningRoadmap r = learningContentService.saveRoadmap(user.id, goal, spec, weeks, content, true);

        auditService.logEvent(
                user.id, user.email, user.role,
                "ROADMAP_SAVE", "ROADMAP", r.id.toString(), "SUCCESS", request,
                Map.of("goal", goal)
        );

        return r;
    }

    @GetMapping("/content/roadmaps")
    public List<LearningRoadmap> getRoadmaps(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return learningContentService.getUserRoadmaps(user.id);
    }

    @PostMapping("/ai/study-plan")
    public Map<String, Object> generateStudyPlan(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> result = ai.call("/study-plan", body);

        auditService.logEvent(
                user.id, user.email, user.role,
                "AI_GENERATE", "STUDY_PLAN", "ai", "SUCCESS", request,
                Map.of("targetCareer", body.getOrDefault("target_career", ""))
        );

        return result;
    }

    @PostMapping("/ai/save-study-plan")
    public StudyPlan saveStudyPlan(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        AppUser user = authenticatedUser(request);
        String title = (String) body.getOrDefault("title", "Healthcare Study Plan");
        String targetCareer = (String) body.getOrDefault("targetCareer", "Healthcare");
        int hours = body.get("hoursPerWeek") instanceof Number n ? n.intValue() : 10;
        Object schedule = body.get("schedule");

        StudyPlan plan = learningContentService.saveStudyPlan(user.id, title, targetCareer, hours, schedule);

        auditService.logEvent(
                user.id, user.email, user.role,
                "STUDY_PLAN_SAVE", "STUDY_PLAN", plan.id.toString(), "SUCCESS", request,
                Map.of("targetCareer", targetCareer)
        );

        return plan;
    }

    @GetMapping("/content/study-plans")
    public List<StudyPlan> getStudyPlans(HttpServletRequest request) {
        AppUser user = authenticatedUser(request);
        return learningContentService.getUserStudyPlans(user.id);
    }

    @PostMapping("/ai/explain")
    public Map<String, Object> explainConcept(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> result = ai.call("/explain", body);

        auditService.logEvent(
                user.id, user.email, user.role,
                "AI_GENERATE", "CONCEPT_EXPLAIN", "ai", "SUCCESS", request,
                Map.of("concept", body.getOrDefault("concept", ""))
        );

        return result;
    }

    @PostMapping("/ai/skill-plan")
    public Map<String, Object> skillImprovementPlan(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> result = ai.call("/skill-plan", body);

        auditService.logEvent(
                user.id, user.email, user.role,
                "AI_GENERATE", "SKILL_PLAN", "ai", "SUCCESS", request,
                Map.of("targetCareer", body.getOrDefault("target_career", ""))
        );

        return result;
    }

    // Role-Specific Dashboards
    @GetMapping("/dashboard/student")
    public Map<String, Object> studentDashboard(HttpServletRequest request) {
        AppUser user = requireRole(request, "STUDENT");
        List<Map<String, Object>> recs = skillCareerService.calculateCareerRecommendations(user);
        Map<String, Object> readiness = skillCareerService.calculateMarketReadiness(user);
        List<Map<String, Object>> opps = opportunityService.getOpportunitiesWithMatch(user, "ALL");
        List<Note> recentNotes = learningContentService.getUserNotes(user.id);
        List<Map<String, Object>> recentPosts = networkingService.getFeed(user.id, "ALL");

        return Map.of(
                "user", user.publicMap(),
                "recommendations", recs.stream().limit(3).toList(),
                "readiness", readiness,
                "opportunities", opps.stream().limit(4).toList(),
                "recentNotes", recentNotes.stream().limit(3).toList(),
                "feed", recentPosts.stream().limit(3).toList(),
                "courses", data.courses,
                "paths", data.learningPaths
        );
    }

    @GetMapping("/dashboard/mentor")
    public Map<String, Object> mentorDashboard(HttpServletRequest request) {
        AppUser mentorUser = requireRole(request, "MENTOR");
        List<MentorshipRequest> pendingRequests = networkingService.getMentorshipRequestsForUser(mentorUser.id, true);
        List<Map<String, Object>> connections = networkingService.getConnectionsForUser(mentorUser.id);
        List<Note> myContent = learningContentService.getUserNotes(mentorUser.id);

        return Map.of(
                "mentor", mentorUser.publicMap(),
                "pendingRequests", pendingRequests,
                "connections", connections,
                "myContent", myContent,
                "courses", data.courses
        );
    }

    @GetMapping("/dashboard/recruiter")
    public Map<String, Object> recruiterDashboard(HttpServletRequest request) {
        AppUser recruiter = requireRole(request, "RECRUITER");
        List<Opportunity> postedOpps = opportunityService.getOpportunitiesWithMatch(recruiter, "ALL").stream()
                .filter(m -> recruiter.id.equals(m.get("publishedByUserId")))
                .map(m -> (Opportunity) null) // placeholder
                .toList();
        List<Map<String, Object>> allOpps = opportunityService.getOpportunitiesWithMatch(recruiter, "ALL");
        List<Map<String, Object>> members = networkingService.getDiscoverableMembers(recruiter.id, null, "STUDENT");

        return Map.of(
                "recruiter", recruiter.publicMap(),
                "opportunities", allOpps,
                "candidatePool", members.stream().limit(10).toList()
        );
    }

    @GetMapping("/dashboard/admin")
    public Map<String, Object> adminDashboard(HttpServletRequest request) {
        requireRole(request, "ADMIN");
        List<AppUser> allUsers = users.all();
        long students = allUsers.stream().filter(u -> "STUDENT".equals(u.role)).count();
        long mentors = allUsers.stream().filter(u -> "MENTOR".equals(u.role)).count();
        long pros = allUsers.stream().filter(u -> "HEALTHCARE_PROFESSIONAL".equals(u.role)).count();
        long recruiters = allUsers.stream().filter(u -> "RECRUITER".equals(u.role)).count();

        List<AuditLog> recentLogs = auditLogRepository.findTop100ByOrderByCreatedAtDesc();

        return Map.of(
                "users", allUsers.stream().map(AppUser::publicMap).toList(),
                "counts", Map.of(
                        "students", students,
                        "mentors", mentors,
                        "professionals", pros,
                        "recruiters", recruiters,
                        "total", allUsers.size()
                ),
                "auditLogs", recentLogs,
                "courses", data.courses
        );
    }

    @GetMapping("/admin/audit-logs")
    public List<AuditLog> auditLogs(HttpServletRequest request) {
        requireRole(request, "ADMIN");
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @PatchMapping("/admin/users/{id}/role")
    public Map<String, Object> changeUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        AppUser admin = requireRole(request, "ADMIN");
        String role = body == null ? null : body.get("role");
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        users.changeRole(id, role.trim());

        auditService.logEvent(
                admin.id, admin.email, admin.role,
                "ROLE_CHANGE", "USER", id, "SUCCESS", request,
                Map.of("newRole", role.trim())
        );

        return users.byId(id).publicMap();
    }

    // Courses & Resources (preserves original endpoints)
    @GetMapping("/courses")
    public List<Map<String, Object>> courses() {
        return data.courses;
    }

    @GetMapping("/paths")
    public List<Map<String, Object>> learningPaths() {
        return data.learningPaths;
    }

    @GetMapping("/resources")
    public List<Map<String, Object>> resources() {
        return data.resources;
    }

    // AI Original Proxy Endpoints
    @GetMapping("/ai/health")
    public Map<String, Object> aiHealth() {
        return ai.get("/health");
    }

    @GetMapping("/research")
    public Map<String, Object> research(
            @RequestParam(name = "q", required = false) String query
    ) {
        if (query == null || query.trim().length() < 2 || query.length() > 180) {
            throw new IllegalArgumentException("Search query must contain 2 to 180 characters.");
        }
        String path = UriComponentsBuilder.fromPath("/research")
                .queryParam("q", query.trim())
                .build()
                .encode()
                .toUriString();
        return ai.get(path);
    }

    @PostMapping("/ai/roadmap")
    public Map<String, Object> roadmap(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> res = ai.call("/roadmap", body);
        auditService.logEvent(user.id, user.email, user.role, "AI_GENERATE", "ROADMAP", "ai", "SUCCESS", request, Map.of("goal", body.getOrDefault("goal", "")));
        return res;
    }

    @PostMapping("/ai/notes")
    public Map<String, Object> notes(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> res = ai.call("/notes", body);
        auditService.logEvent(user.id, user.email, user.role, "AI_GENERATE", "NOTES", "ai", "SUCCESS", request, Map.of("topic", body.getOrDefault("topic", "")));
        return res;
    }

    @PostMapping("/ai/quiz")
    public Map<String, Object> quiz(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        Map<String, Object> res = ai.call("/quiz", body);
        auditService.logEvent(user.id, user.email, user.role, "AI_GENERATE", "QUIZ", "ai", "SUCCESS", request, Map.of("topic", body.getOrDefault("topic", "")));
        return res;
    }

    @PostMapping("/ai/presentation")
    public ResponseEntity<byte[]> presentation(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        validateBody(body);
        AppUser user = authenticatedUser(request);
        byte[] bytes = ai.presentation(body);
        auditService.logEvent(user.id, user.email, user.role, "AI_GENERATE", "PRESENTATION", "ai", "SUCCESS", request, Map.of("topic", body.getOrDefault("topic", "")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=healthskill-presentation.pptx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .body(bytes);
    }

    private AppUser requireRole(HttpServletRequest request, String role) {
        AppUser user = authenticatedUser(request);
        if (!role.equals(user.role) && !"ADMIN".equals(user.role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
        }
        return user;
    }

    private AppUser authenticatedUser(HttpServletRequest request) {
        Object user = request.getAttribute("authUser");
        if (user instanceof AppUser appUser) {
            return appUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
    }

    private AppUser optionalAuthenticatedUser(HttpServletRequest request) {
        Object user = request.getAttribute("authUser");
        if (user instanceof AppUser appUser) {
            return appUser;
        }
        return null;
    }

    private void validateBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Request body must not be empty.");
        }
    }
}
