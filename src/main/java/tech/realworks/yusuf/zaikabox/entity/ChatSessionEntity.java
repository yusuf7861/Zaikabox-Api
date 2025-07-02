package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_sessions")
public class ChatSessionEntity {

    public enum SessionStatus {
        ACTIVE,         // Ongoing conversation
        ENDED,          // User ended the chat
        TIMED_OUT,      // Session timed out due to inactivity
        TRANSFERRED     // Transferred to human agent
    }

    @Id
    private String id;

    private String userId;                 // User ID if authenticated, null for anonymous
    private LocalDateTime startTime;        // When the session started
    private LocalDateTime lastActivityTime; // Last message timestamp
    private SessionStatus status;           // Current session status
    private String deviceInfo;              // User's device information
    private List<String> messageIds;        // References to messages in this session
    private String initialQuery;            // The first question/message from the user
    private String primaryIntent;           // Main intent of the conversation
    private boolean feedbackProvided;       // Whether user provided feedback
    private Integer satisfactionRating;     // User satisfaction rating (1-5)

    // Helper method to add a message reference
    public void addMessageId(String messageId) {
        if (messageIds == null) {
            messageIds = new ArrayList<>();
        }
        messageIds.add(messageId);
        this.lastActivityTime = LocalDateTime.now();
    }
}
