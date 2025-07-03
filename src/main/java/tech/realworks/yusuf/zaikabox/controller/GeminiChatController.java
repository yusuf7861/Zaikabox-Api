package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.service.GeminiChatService;

@RestController
@RequestMapping("/api/v1/chat/gemini")
@Tag(name = "Chat with Gemini", description = "API for chatting with Gemini AI")
public class GeminiChatController {

    private final GeminiChatService geminiChatService;

    @Autowired
    public GeminiChatController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    @Operation(
            summary = "Send a message to Gemini and receive a response",
            description = "Accepts a user message and returns Gemini's response."
    )
    @ApiResponse(responseCode = "200", description = "Successful response from Gemini")
    @PostMapping
    public ResponseEntity<String> chatWithGemini(@RequestBody String userMessage) {
        try {
            String response = geminiChatService.getGeminiResponse(userMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

