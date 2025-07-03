package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessageEntity {

    public enum MessageType {
        USER,       // Message from user
        BOT,        // Message from chatbot
        SYSTEM      // System message (e.g., session start/end)
    }

    @Id
    private String id;

    private String sessionId;      // Chat session identifier
    private String userId;         // User identifier (null for anonymous)
    private String message;        // Message content
    private MessageType type;      // Type of message
    private LocalDateTime timestamp; // When the message was sent
    private String intent;         // Detected intent (e.g., ORDER_HELP, FAQ, DIETARY_QUESTION)
    private boolean resolved;      // Whether the user's query was resolved
    private String metadata;       // Additional context (e.g., order ID, food item)
}
