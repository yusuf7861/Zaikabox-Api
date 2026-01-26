package tech.realworks.yusuf.zaikabox.entity;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Represents a user entity in the ZaikaBox application.
 * This class is used to store user-related information in the MongoDB database.
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;
    @Size(min = 3, max = 20)
    private String name;
    private String email;
    private String password;
    private Role role;
}
