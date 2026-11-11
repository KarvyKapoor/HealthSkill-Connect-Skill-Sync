package com.healthskillconnect.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.*;
import com.healthskillconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillCareerService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final CareerPathRepository careerPathRepository;
    private final OpportunityRepository opportunityRepository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    public SkillCareerService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            CareerPathRepository careerPathRepository,
            OpportunityRepository opportunityRepository,
            ObjectMapper objectMapper) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.careerPathRepository = careerPathRepository;
        this.opportunityRepository = opportunityRepository;
        this.objectMapper = objectMapper;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAllByOrderByNameAsc();
    }

    public List<UserSkill> getUserSkills(String userId) {
        return userSkillRepository.findByUserId(userId);
    }

    @Transactional
    public UserSkill addUserSkill(String userId, String skillName, String proficiency, String evidenceType, String evidenceTitle) {
        String prof = (proficiency != null && !proficiency.isBlank()) ? proficiency.trim() : "Intermediate";
        String evType = (evidenceType != null && !evidenceType.isBlank()) ? evidenceType.trim() : "SELF_REPORTED";

        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillName.trim());
        String skillId = existingSkill.map(s -> s.id).orElse("skill-custom-" + UUID.randomUUID().toString().substring(0, 6));

        UserSkill us = new UserSkill(
                userId,
                skillId,
                skillName.trim(),
                prof,
                evType,
                evidenceTitle,
                !"SELF_REPORTED".equalsIgnoreCase(evType) // Auto-verified if certificate/assessment provided
        );
        return userSkillRepository.save(us);
    }

    @Transactional
    public void deleteUserSkill(Long id) {
        userSkillRepository.deleteById(id);
    }

    public List<CareerPath> getAllCareerPaths() {
        return careerPathRepository.findAll();
    }

    public Optional<CareerPath> getCareerPathById(String id) {
        return careerPathRepository.findById(id);
    }

    /**
     * Algorithmic explainable career recommendation engine.
     */
    public List<Map<String, Object>> calculateCareerRecommendations(AppUser user) {
        List<CareerPath> allCareers = careerPathRepository.findAll();
        List<UserSkill> userSkillsList = userSkillRepository.findByUserId(user.id);
        Set<String> userSkillNames = new HashSet<>();
        for (UserSkill us : userSkillsList) {
            userSkillNames.add(us.skillName.toLowerCase().trim());
        }
        if (user.skills != null) {
            for (String s : user.skills) {
                userSkillNames.add(s.toLowerCase().trim());
            }
        }

        Set<String> userInterests = new HashSet<>();
        if (user.learningInterests != null) {
            for (String i : user.learningInterests) {
                userInterests.add(i.toLowerCase().trim());
            }
        }
        String userSpec = (user.specialization != null) ? user.specialization.toLowerCase().trim() : "";

        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (CareerPath cp : allCareers) {
            List<String> requiredSkills = parseList(cp.requiredSkills);
            List<String> requiredSpecializations = parseList(cp.requiredSpecializations);
            List<String> requiredQuals = parseList(cp.requiredQualifications);

            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();

            for (String reqSkill : requiredSkills) {
                boolean found = false;
                for (String us : userSkillNames) {
                    if (us.contains(reqSkill.toLowerCase()) || reqSkill.toLowerCase().contains(us)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    matchedSkills.add(reqSkill);
                } else {
                    missingSkills.add(reqSkill);
                }
            }

            // Calculate skill match score (0-60 points)
            double skillScore = requiredSkills.isEmpty() ? 30.0 :
                    ((double) matchedSkills.size() / requiredSkills.size()) * 60.0;

            // Calculate specialization match score (0-25 points)
            double specScore = 10.0;
            if (!userSpec.isEmpty()) {
                for (String rs : requiredSpecializations) {
                    if (userSpec.contains(rs.toLowerCase()) || rs.toLowerCase().contains(userSpec)) {
                        specScore = 25.0;
                        break;
                    }
                }
            }

            // Calculate interest bonus (0-15 points)
            double interestScore = 5.0;
            for (String ui : userInterests) {
                if (cp.category.toLowerCase().contains(ui) || cp.name.toLowerCase().contains(ui)) {
                    interestScore = 15.0;
                    break;
                }
            }

            int matchPercentage = Math.min(98, (int) Math.round(skillScore + specScore + interestScore));

            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("careerId", cp.id);
            rec.put("careerName", cp.name);
            rec.put("category", cp.category);
            rec.put("description", cp.description);
            rec.put("matchPercentage", matchPercentage);
            rec.put("matchedSkills", matchedSkills);
            rec.put("missingSkills", missingSkills);
            rec.put("requiredQualifications", requiredQuals);
            rec.put("recommendedCertifications", parseList(cp.recommendedCertifications));
            rec.put("averageSalaryRange", cp.averageSalaryRange);
            rec.put("marketOutlook", cp.marketOutlook);

            String explanation;
            if (matchPercentage >= 75) {
                explanation = "Strong match based on your background in " + user.specialization + " and existing competencies in " + (matchedSkills.isEmpty() ? "healthcare fundamentals" : String.join(", ", matchedSkills.stream().limit(3).toList())) + ".";
            } else if (matchPercentage >= 50) {
                explanation = "Good alignment with your interests. Developing " + (missingSkills.isEmpty() ? "further credentials" : missingSkills.get(0)) + " will accelerate entry into this role.";
            } else {
                explanation = "Emerging exploration path. Developing core skills like " + (missingSkills.isEmpty() ? "domain knowledge" : String.join(", ", missingSkills.stream().limit(2).toList())) + " is recommended.";
            }
            rec.put("explanation", explanation);

            recommendations.add(rec);
        }

        recommendations.sort((a, b) -> Integer.compare((int) b.get("matchPercentage"), (int) a.get("matchPercentage")));
        return recommendations;
    }

    /**
     * Market Readiness Intelligence comparing student skills & qualifications with real platform opportunities.
     */
    public Map<String, Object> calculateMarketReadiness(AppUser user) {
        List<Opportunity> opportunities = opportunityRepository.findAll();
        List<UserSkill> userSkillsList = userSkillRepository.findByUserId(user.id);
        Set<String> userSkillNames = new HashSet<>();
        for (UserSkill us : userSkillsList) {
            userSkillNames.add(us.skillName.toLowerCase().trim());
        }
        if (user.skills != null) {
            for (String s : user.skills) {
                userSkillNames.add(s.toLowerCase().trim());
            }
        }

        Map<String, Integer> skillDemandCounter = new LinkedHashMap<>();
        int totalMatches = 0;
        int totalRequirements = 0;

        for (Opportunity opp : opportunities) {
            List<String> oppSkills = parseList(opp.requiredSkillsJson);
            for (String s : oppSkills) {
                String normalized = s.trim();
                skillDemandCounter.put(normalized, skillDemandCounter.getOrDefault(normalized, 0) + 1);
                totalRequirements++;
                if (userSkillNames.contains(normalized.toLowerCase())) {
                    totalMatches++;
                }
            }
        }

        // Sort in-demand skills by frequency
        List<Map<String, Object>> topInDemandSkills = skillDemandCounter.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("skill", e.getKey());
                    m.put("demandCount", e.getValue());
                    m.put("possessed", userSkillNames.contains(e.getKey().toLowerCase()));
                    return m;
                })
                .collect(Collectors.toList());

        int readinessScore = totalRequirements > 0
                ? Math.min(100, Math.max(25, (int) Math.round(((double) totalMatches / totalRequirements) * 100 * 1.5)))
                : 60;

        String readinessTier;
        if (readinessScore >= 75) {
            readinessTier = "Strong alignment";
        } else if (readinessScore >= 45) {
            readinessTier = "Developing";
        } else {
            readinessTier = "Significant gaps";
        }

        List<String> recommendedNextSkills = topInDemandSkills.stream()
                .filter(s -> !(Boolean) s.get("possessed"))
                .map(s -> (String) s.get("skill"))
                .limit(4)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("readinessScore", readinessScore);
        result.put("readinessTier", readinessTier);
        result.put("opportunitiesAnalyzed", opportunities.size());
        result.put("inDemandSkills", topInDemandSkills);
        result.put("recommendedNextSkills", recommendedNextSkills);
        result.put("qualificationCompatibility", user.specialization != null && !user.specialization.isBlank() ? "High" : "Moderate");
        result.put("userSkillCount", userSkillNames.size());
        result.put("freshness", "Active database live assessment (" + LocalDateTime.now().toLocalDate() + ")");

        return result;
    }

    private List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        try {
            if (raw.trim().startsWith("[")) {
                return objectMapper.readValue(raw, STRING_LIST_TYPE);
            }
        } catch (Exception ignored) {}

        // Comma-separated fallback
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
