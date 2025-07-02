package tech.realworks.yusuf.zaikabox.controller;

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
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatSessionRepository chatSessionRepository;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
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

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getSession(@PathVariable String sessionId) {
        return chatSessionRepository.findById(sessionId)
                .map(session -> ResponseEntity.ok(convertToResponse(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<List<ChatSessionResponse>> getUserSessions(@PathVariable String userId) {
        List<ChatSessionEntity> sessions = chatSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        List<ChatSessionResponse> responses = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/session/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        chatbotService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/session/{sessionId}/feedback")
    public ResponseEntity<Void> provideFeedback(
            @PathVariable String sessionId,
            @RequestBody FeedbackRequest feedback) {
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
