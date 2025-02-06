package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Email(message = "Requires correct email format")
    private String email;

    @Size(min = 8, message = "Username must be at least 8 symbols")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 symbols")
    private String password;
}
