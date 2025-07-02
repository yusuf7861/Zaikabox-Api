package tech.realworks.yusuf.zaikabox.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "dietary_preferences")
public class DietaryPreferenceEntity {

    @Id
    private String id;

    private String userId;                // User ID these preferences belong to
    private boolean vegetarian;            // Vegetarian preference
    private boolean vegan;                 // Vegan preference
    private boolean glutenFree;            // Gluten-free preference
    private boolean dairyFree;             // Dairy-free preference
    private List<String> allergies;        // List of food allergies
    private List<String> dislikedIngredients; // Ingredients user dislikes
    private List<String> favoriteIngredients; // Ingredients user likes
    private List<String> preferredCuisines;   // Preferred cuisine types
    private int spiceLevel;                // Preferred spice level (1-5)
    private String dietType;               // Diet type (e.g., KETO, PALEO)
    private String additionalNotes;        // Any additional dietary notes
}
