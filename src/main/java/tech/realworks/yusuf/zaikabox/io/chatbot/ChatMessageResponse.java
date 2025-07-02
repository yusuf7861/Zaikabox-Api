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
public class ChatMessageResponse {
    private String id;             // Message ID
    private String sessionId;      // Session ID
    private String message;        // Bot response message
    private LocalDateTime timestamp; // When the response was generated
    private boolean isComplete;    // Whether this is a complete response or more is coming
    private String suggestionType; // Type of suggestion if any (e.g. MENU_ITEM, FAQ)
    private String[] suggestions;  // Optional quick reply suggestions
}
