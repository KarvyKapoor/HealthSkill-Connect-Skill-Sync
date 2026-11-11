package com.healthskillconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OnboardingService {

    private final OnboardingResponseRepository onboardingRepository;
    private final UserService userService;
    private final SkillCareerService skillCareerService;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;

    public OnboardingService(
            OnboardingResponseRepository onboardingRepository,
            UserService userService,
            SkillCareerService skillCareerService,
            UserSkillRepository userSkillRepository,
            ObjectMapper objectMapper) {
        this.onboardingRepository = onboardingRepository;
        this.userService = userService;
        this.skillCareerService = skillCareerService;
        this.userSkillRepository = userSkillRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<OnboardingResponse> getLatestResponse(String userId) {
        return onboardingRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Map<String, Object> submitQuestionnaire(AppUser user, Map<String, Object> body) {
        OnboardingResponse response = new OnboardingResponse();
        response.userId = user.id;

        String qualification = (String) body.getOrDefault("currentQualification", "");
        String degree = (String) body.getOrDefault("degreeOrCourse", "");
        String specialization = (String) body.getOrDefault("specialization", user.specialization);
        String institution = (String) body.getOrDefault("institution", user.institution);
        String academicYear = (String) body.getOrDefault("academicYearOrSemester", user.experienceLevel);
        String experienceSummary = (String) body.getOrDefault("experienceSummary", "");
        String shortTerm = (String) body.getOrDefault("shortTermGoals", "");
        String longTerm = (String) body.getOrDefault("longTermGoals", "");

        response.currentQualification = qualification;
        response.degreeOrCourse = degree;
        response.specialization = specialization;
        response.institution = institution;
        response.academicYearOrSemester = academicYear;
        response.experienceSummary = experienceSummary;
        response.shortTermGoals = shortTerm;
        response.longTermGoals = longTerm;

        try {
            if (body.get("healthcareInterests") != null) {
                response.healthcareInterestsJson = objectMapper.writeValueAsString(body.get("healthcareInterests"));
            }
            if (body.get("careerPreferences") != null) {
                response.careerPreferencesJson = objectMapper.writeValueAsString(body.get("careerPreferences"));
            }
            if (body.get("existingSkills") != null) {
                response.existingSkillsJson = objectMapper.writeValueAsString(body.get("existingSkills"));
            }
            if (body.get("learningPreferences") != null) {
                response.learningPreferencesJson = objectMapper.writeValueAsString(body.get("learningPreferences"));
            }
        } catch (Exception ignored) {}

        onboardingRepository.save(response);

        // Update user profile
        user.specialization = (specialization != null && !specialization.isBlank()) ? specialization : user.specialization;
        user.institution = (institution != null && !institution.isBlank()) ? institution : user.institution;
        user.degree = (degree != null && !degree.isBlank()) ? degree : user.degree;
        user.experienceLevel = (academicYear != null && !academicYear.isBlank()) ? academicYear : user.experienceLevel;
        user.onboardingCompleted = true;

        // Process interests
        if (body.get("healthcareInterests") instanceof List<?> list) {
            user.learningInterests = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && !s.isBlank()) {
                    user.learningInterests.add(s.trim());
                }
            }
        }

        // Process skills
        if (body.get("existingSkills") instanceof List<?> skillList) {
            user.skills = new ArrayList<>();
            for (Object item : skillList) {
                if (item instanceof Map<?, ?> m) {
                    Object nameObj = m.get("name");
                    String sName = nameObj instanceof String s ? s : null;
                    Object profObj = m.get("proficiency");
                    String prof = profObj instanceof String p && !p.isBlank() ? p.trim() : "Intermediate";
                    if (sName != null && !sName.isBlank()) {
                        user.skills.add(sName.trim());
                        skillCareerService.addUserSkill(user.id, sName.trim(), prof, "SELF_REPORTED", "Onboarding Questionnaire");
                    }
                } else if (item instanceof String s && !s.isBlank()) {
                    user.skills.add(s.trim());
                    skillCareerService.addUserSkill(user.id, s.trim(), "Intermediate", "SELF_REPORTED", "Onboarding Questionnaire");
                }
            }
        }

        user.learningProgress = Math.max(user.learningProgress, 25);
        userService.save(user);

        // Calculate initial recommendations and readiness
        List<Map<String, Object>> recommendations = skillCareerService.calculateCareerRecommendations(user);
        Map<String, Object> readiness = skillCareerService.calculateMarketReadiness(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Onboarding completed successfully.");
        result.put("user", user.publicMap());
        result.put("recommendations", recommendations);
        result.put("readiness", readiness);
        return result;
    }
}
