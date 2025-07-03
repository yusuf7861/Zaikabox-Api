package tech.realworks.yusuf.zaikabox.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.AuditEntity;
import tech.realworks.yusuf.zaikabox.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final HttpServletRequest request;

    public void logEvent(String userId, String email, String eventType, String details, boolean success) {
        AuditEntity auditLog = AuditEntity.builder()
                .userId(userId)
                .email(email)
                .eventType(eventType)
                .details(details)
                .ipAddress(getClientIp())
                .userAgent(request.getHeader("User-Agent"))
                .success(success)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(auditLog);
    }

    public void logLoginEvent(String userId, String email, boolean success, String details) {
        logEvent(userId, email, AuditEntity.EventType.LOGIN.name(), details, success);
    }

    public void logRegistrationEvent(String userId, String email, boolean success, String details) {
        logEvent(userId, email, AuditEntity.EventType.REGISTER.name(), details, success);
    }

    public void logPasswordResetEvent(String userId, String email, boolean success, String details) {
        logEvent(userId, email, AuditEntity.EventType.PASSWORD_RESET.name(), details, success);
    }

    public void logProfileUpdateEvent(String userId, String email, boolean success, String details) {
        logEvent(userId, email, AuditEntity.EventType.PROFILE_UPDATE.name(), details, success);
    }

    public void logLogoutEvent(String userId, String email) {
        logEvent(userId, email, AuditEntity.EventType.LOGOUT.name(), "User logged out", true);
    }

    // Retrieval methods to get audit logs
    public List<AuditEntity> getAuditLogsByUser(String userId) {
        return auditRepository.findByUserId(userId);
    }

    public List<AuditEntity> getAuditLogsByEmail(String email) {
        return auditRepository.findByEmail(email);
    }

    public List<AuditEntity> getAuditLogsByEventType(String eventType) {
        return auditRepository.findByEventType(eventType);
    }

    public List<AuditEntity> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditRepository.findByTimestampBetween(start, end);
    }

    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
