package tech.realworks.yusuf.zaikabox.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GeminiChatService {

    private final Client client;

    public GeminiChatService(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client.builder().apiKey(apiKey).build();
    }

    public String getGeminiResponse(String userMessage) {

        // Validate input
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("Empty or null user message received");
            return "Please provide a valid message.";
        }

        try {
            // Build content for Gemini API
            Content content = Content.builder()
                    .role("user")
                    .parts(ImmutableList.of(Part.fromText(userMessage)))
                    .build();

            // Configure response format
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("text/plain")
                    .build();

            // Use the correct model (update if gemini-2.0-flash-lite is not valid)
            String model = "gemini-1.5-flash"; // Adjust based on Gemini API documentation

            StringBuilder responseBuilder = new StringBuilder();
            try (ResponseStream<GenerateContentResponse> responseStream = client.models.generateContentStream(model, ImmutableList.of(content), config)) {
                for (GenerateContentResponse response : responseStream) {
                    // Safely check for valid response content
                    if (response.candidates().isPresent() && !response.candidates().get().isEmpty()) {
                        var candidate = response.candidates().get().get(0);
                        if (candidate.content().isPresent() && candidate.content().get().parts().isPresent()) {
                            for (Part part : candidate.content().get().parts().get()) {
                                if (part.text().isPresent()) {
                                    responseBuilder.append(part.text().get());
                                }
                            }
                        }
                    }
                }
            }

            // Apply optimized filtering
            String rawResponse = responseBuilder.toString();
            if (rawResponse.isEmpty()) {
                log.warn("No content received from Gemini API for message: {}", userMessage);
                return "No response generated.";
            }

            // Compile regex for efficient filtering
            Pattern pattern = Pattern.compile(
                    "\\s+|```[^```]*```|`[^`]*`|[*_#`~\\[\\]>-]",
                    Pattern.DOTALL
            );
            String result = getString(pattern, rawResponse);
            log.debug("Filtered response: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Error processing Gemini API request for message: {}", userMessage, e);
            return "Sorry, an error occurred while processing your request. Please try again later.";
        }
    }

    private static String getString(Pattern pattern, String rawResponse) {
        StringBuilder filtered = new StringBuilder();
        Matcher matcher = pattern.matcher(rawResponse);
        int lastPos = 0;

        // Replace matches with space (for whitespace) or empty string (for markdown)
        while (matcher.find()) {
            filtered.append(rawResponse, lastPos, matcher.start());
            String match = matcher.group();
            filtered.append(match.matches("\\s+") ? " " : "");
            lastPos = matcher.end();
        }
        filtered.append(rawResponse, lastPos, rawResponse.length());

        String result = filtered.toString().trim();
        return result;
    }
}
