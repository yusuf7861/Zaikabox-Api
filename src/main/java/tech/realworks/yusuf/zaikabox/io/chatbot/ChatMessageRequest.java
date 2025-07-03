package tech.realworks.yusuf.zaikabox.io.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequest {
    private String sessionId;  // Optional, null for new session
    private String userId;     // User ID if authenticated, null for anonymous
    private String message;    // The actual message content
    private String deviceInfo; // Information about the user's device
}
