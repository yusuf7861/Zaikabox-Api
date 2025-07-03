package tech.realworks.yusuf.zaikabox.io.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackRequest {
    private int rating;            // User satisfaction rating (1-5)
    private String comment;        // Optional feedback comment
    private String improvementArea; // Area for improvement (optional)
}
