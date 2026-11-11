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
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository applicationRepository;
    private final SavedOpportunityRepository savedOpportunityRepository;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    public OpportunityService(
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository applicationRepository,
            SavedOpportunityRepository savedOpportunityRepository,
            UserSkillRepository userSkillRepository,
            ObjectMapper objectMapper) {
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.savedOpportunityRepository = savedOpportunityRepository;
        this.userSkillRepository = userSkillRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> getOpportunitiesWithMatch(AppUser user, String typeFilter) {
        List<Opportunity> list = (typeFilter != null && !typeFilter.isBlank() && !"ALL".equalsIgnoreCase(typeFilter))
                ? opportunityRepository.findByType(typeFilter.toUpperCase().trim())
                : opportunityRepository.findAllByOrderByCreatedAtDesc();

        Set<String> userSkills = new HashSet<>();
        if (user != null) {
            List<UserSkill> usList = userSkillRepository.findByUserId(user.id);
            for (UserSkill us : usList) {
                userSkills.add(us.skillName.toLowerCase().trim());
            }
            if (user.skills != null) {
                for (String s : user.skills) {
                    userSkills.add(s.toLowerCase().trim());
                }
            }
        }

        Set<String> savedOppIds = (user != null)
                ? savedOpportunityRepository.findByUserIdOrderBySavedAtDesc(user.id).stream()
                .map(s -> s.opportunityId).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<String> appliedOppIds = (user != null)
                ? applicationRepository.findByUserIdOrderByAppliedAtDesc(user.id).stream()
                .map(a -> a.opportunityId).collect(Collectors.toSet())
                : Collections.emptySet();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Opportunity opp : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", opp.id);
            map.put("title", opp.title);
            map.put("organization", opp.organization);
            map.put("type", opp.type);
            map.put("description", opp.description);
            map.put("eligibilitySummary", opp.eligibilitySummary);
            map.put("requiredQualification", opp.requiredQualification);
            map.put("requiredSpecialization", opp.requiredSpecialization);
            List<String> requiredSkills = parseSkills(opp.requiredSkillsJson);
            map.put("requiredSkills", requiredSkills);
            map.put("location", opp.location);
            map.put("workMode", opp.workMode);
            map.put("duration", opp.duration);
            map.put("stipendOrSalary", opp.stipendOrSalary);
            map.put("deadline", opp.deadline);
            map.put("officialUrl", opp.officialUrl);
            map.put("verificationStatus", opp.verificationStatus);
            map.put("isSaved", savedOppIds.contains(opp.id));
            map.put("isApplied", appliedOppIds.contains(opp.id));

            // Calculate match percentage and skill alignment
            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();
            for (String req : requiredSkills) {
                if (userSkills.contains(req.toLowerCase().trim())) {
                    matchedSkills.add(req);
                } else {
                    missingSkills.add(req);
                }
            }

            int matchPercent = 60;
            if (!requiredSkills.isEmpty()) {
                double matchFraction = (double) matchedSkills.size() / requiredSkills.size();
                matchPercent = (int) Math.round(matchFraction * 80 + (user != null && user.specialization != null && opp.requiredSpecialization != null && opp.requiredSpecialization.toLowerCase().contains(user.specialization.toLowerCase()) ? 20 : 10));
            }
            map.put("matchPercentage", Math.min(99, Math.max(30, matchPercent)));
            map.put("matchedSkills", matchedSkills);
            map.put("missingSkills", missingSkills);

            result.add(map);
        }

        return result;
    }

    @Transactional
    public Opportunity createOpportunity(String title, String org, String type, String desc, String elig, String qual, String spec, List<String> skills, String loc, String mode, String duration, String stipend, String deadline, String url, String userId) {
        String oppId = "opp-" + UUID.randomUUID().toString().substring(0, 8);
        String skillsJson = "";
        try {
            skillsJson = objectMapper.writeValueAsString(skills != null ? skills : Collections.emptyList());
        } catch (Exception ignored) {}

        Opportunity opp = new Opportunity(
                oppId,
                title.trim(),
                org.trim(),
                type != null ? type.toUpperCase().trim() : "INTERNSHIP",
                elig,
                qual,
                spec,
                skillsJson,
                loc,
                mode != null ? mode.toUpperCase().trim() : "ON_SITE",
                duration,
                stipend,
                deadline,
                url,
                "ORGANIZATION_POSTED"
        );
        opp.publishedByUserId = userId;
        return opportunityRepository.save(opp);
    }

    @Transactional
    public boolean toggleSave(String userId, String oppId) {
        Optional<SavedOpportunity> existing = savedOpportunityRepository.findByUserIdAndOpportunityId(userId, oppId);
        if (existing.isPresent()) {
            savedOpportunityRepository.delete(existing.get());
            return false;
        } else {
            savedOpportunityRepository.save(new SavedOpportunity(userId, oppId));
            return true;
        }
    }

    @Transactional
    public OpportunityApplication apply(String userId, String oppId, String name, String email, String spec, String coverNote) {
        Optional<OpportunityApplication> existing = applicationRepository.findByOpportunityIdAndUserId(oppId, userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        OpportunityApplication app = new OpportunityApplication(
                oppId,
                userId,
                name.trim(),
                email.trim(),
                spec,
                coverNote
        );
        return applicationRepository.save(app);
    }

    public List<OpportunityApplication> getApplicationsForUser(String userId) {
        return applicationRepository.findByUserIdOrderByAppliedAtDesc(userId);
    }

    public List<OpportunityApplication> getApplicationsForOpportunity(String oppId) {
        return applicationRepository.findByOpportunityIdOrderByAppliedAtDesc(oppId);
    }

    private List<String> parseSkills(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, STRING_LIST_TYPE);
            }
        } catch (Exception ignored) {}
        return Arrays.stream(json.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
