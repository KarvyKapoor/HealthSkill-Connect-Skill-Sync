package com.healthskillconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.model.AuditLog;
import com.healthskillconnect.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void logEvent(String actorUserId, String actorEmail, String actorRole,
                         String eventType, String targetEntityType, String targetEntityId,
                         String resultStatus, HttpServletRequest request, Map<String, Object> details) {
        try {
            String ip = null;
            if (request != null) {
                ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
            }

            String detailsJson = null;
            if (details != null && !details.isEmpty()) {
                // Ensure sensitive tokens or passwords are never serialized
                Map<String, Object> sanitized = new java.util.HashMap<>(details);
                sanitized.remove("password");
                sanitized.remove("passwordHash");
                sanitized.remove("token");
                detailsJson = objectMapper.writeValueAsString(sanitized);
            }

            AuditLog entry = new AuditLog(
                    actorUserId,
                    actorEmail,
                    actorRole,
                    eventType,
                    targetEntityType,
                    targetEntityId,
                    resultStatus != null ? resultStatus : "SUCCESS",
                    ip,
                    detailsJson
            );

            auditLogRepository.save(entry);
            log.info("AUDIT: [{}] by user:{} (role:{}) on {}:{} -> {}",
                    eventType, actorUserId, actorRole, targetEntityType, targetEntityId, resultStatus);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage());
        }
    }
}
