package tech.realworks.yusuf.zaikabox.io.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatSessionResponse {
    private String id;                     // Session ID
    private String userId;                 // User ID if authenticated
    private LocalDateTime startTime;       // When the session started
    private LocalDateTime lastActivityTime; // Last activity timestamp
    private String status;                 // Current session status
    private String initialQuery;           // First message in the conversation
}
