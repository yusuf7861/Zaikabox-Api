package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "faq_items")
public class FAQEntity {

    @Id
    private String id;

    @Indexed
    private String question;              // The FAQ question

    private String answer;                // The answer to the question
    private List<String> keywords;        // Keywords for better matching
    private String category;              // Category (e.g., ORDERING, DELIVERY, PAYMENT)
    private int popularity;               // How often this question is asked
    private LocalDateTime lastUpdated;    // When the FAQ was last updated
    private boolean isActive;             // Whether this FAQ is active
}
