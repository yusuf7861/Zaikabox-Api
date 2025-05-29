package tech.realworks.yusuf.zaikabox.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
    @Email(message = "Enter valid email")
    private String email;
    @Size(min = 8, max = 16, message = "Password cannot be null")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String password;

}
