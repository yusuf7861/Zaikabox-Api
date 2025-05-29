package tech.realworks.yusuf.zaikabox.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a food item entity in the ZaikaBox application.
 * This class is used to store food-related information in the MongoDB database.
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(
        collection = "foods"
)
public class FoodEntity {
    @Id
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
}
