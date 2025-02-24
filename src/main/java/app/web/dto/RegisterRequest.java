package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Email(message = "Requires correct email format")
    private String email;

    @NotNull
    @Size(min = 4, message = "Username must be at least 4 symbols")
    private String username;

    @NotNull
    @Size(min = 8, message = "Password must be at least 8 symbols")
    private String password;
}
