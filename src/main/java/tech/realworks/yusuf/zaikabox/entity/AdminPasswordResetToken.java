package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admin_password_reset_tokens")
public class AdminPasswordResetToken {
    @Id
    private String id;

    private String email;

    private String hashedOtp;

    @Indexed(name = "admin_password_reset_ttl_idx", expireAfterSeconds = 0)
    private Instant expiresAt;

    private boolean consumed;

    private int attempts;

    private Instant createdAt;
}
