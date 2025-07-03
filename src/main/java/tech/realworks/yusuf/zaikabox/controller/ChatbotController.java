package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.ChatMessageEntity;
import tech.realworks.yusuf.zaikabox.entity.ChatSessionEntity;
import tech.realworks.yusuf.zaikabox.io.chatbot.ChatMessageRequest;
import tech.realworks.yusuf.zaikabox.io.chatbot.ChatMessageResponse;
import tech.realworks.yusuf.zaikabox.io.chatbot.ChatSessionResponse;
import tech.realworks.yusuf.zaikabox.io.chatbot.FeedbackRequest;
import tech.realworks.yusuf.zaikabox.repository.ChatSessionRepository;
import tech.realworks.yusuf.zaikabox.service.ChatbotService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot", description = "APIs for interacting with the chatbot system")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatSessionRepository chatSessionRepository;

    @Operation(summary = "Send message to chatbot", description = "Sends a user message to the chatbot and returns the bot's response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message processed successfully",
                     content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid message request")
    })
    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Parameter(description = "Chat message details") @RequestBody ChatMessageRequest request) {
        log.info("Received chat message: {}", request);

        ChatMessageEntity botResponse = chatbotService.processUserMessage(
                request.getSessionId(),
                request.getUserId(),
                request.getMessage()
        );

        ChatMessageResponse response = ChatMessageResponse.builder()
                .sessionId(botResponse.getSessionId())
                .message(botResponse.getMessage())
                .timestamp(botResponse.getTimestamp())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get chat session", description = "Retrieves details of a specific chat session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session found",
                     content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getSession(
            @Parameter(description = "ID of the chat session") @PathVariable String sessionId) {
        return chatSessionRepository.findById(sessionId)
                .map(session -> ResponseEntity.ok(convertToResponse(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user's chat sessions", description = "Retrieves all chat sessions for a specific user")
    @ApiResponse(responseCode = "200", description = "List of user's chat sessions",
                 content = @Content(schema = @Schema(implementation = ChatSessionResponse.class)))
    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<List<ChatSessionResponse>> getUserSessions(
            @Parameter(description = "ID of the user") @PathVariable String userId) {
        List<ChatSessionEntity> sessions = chatSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        List<ChatSessionResponse> responses = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Close chat session", description = "Marks a chat session as closed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session closed successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/session/{sessionId}/close")
    public ResponseEntity<Void> closeSession(
            @Parameter(description = "ID of the session to close") @PathVariable String sessionId) {
        chatbotService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Submit feedback for chat session", description = "Records user feedback for a chat session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feedback recorded successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/session/{sessionId}/feedback")
    public ResponseEntity<Void> provideFeedback(
            @Parameter(description = "ID of the session to rate") @PathVariable String sessionId,
            @Parameter(description = "Feedback details") @RequestBody FeedbackRequest feedback) {
        chatbotService.rateSession(sessionId, feedback.getRating());
        return ResponseEntity.ok().build();
    }

    private ChatSessionResponse convertToResponse(ChatSessionEntity session) {
        return ChatSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .startTime(session.getStartTime())
                .lastActivityTime(session.getLastActivityTime())
                .status(session.getStatus().name())
                .initialQuery(session.getInitialQuery())
                .build();
    }
}
