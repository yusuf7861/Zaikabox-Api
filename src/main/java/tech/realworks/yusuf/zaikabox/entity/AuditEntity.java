package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
@Persistent
public class AuditEntity {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String email;

    @Indexed
    private String eventType;  // LOGIN, REGISTER, etc.

    private String details;
    private String ipAddress;
    private String userAgent;
    private boolean success;

    @CreatedDate
    @Indexed
    private LocalDateTime timestamp = LocalDateTime.now(); // Default to current time

    public enum EventType {
        LOGIN,
        REGISTER,
        PASSWORD_RESET,
        PROFILE_UPDATE,
        LOGOUT
    }
}
