package app.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull
    @Size(min = 8, message = "Username must be at least 8 characters")
    private String username;

    @NotNull
    @Pattern(regexp = "\\d{8}", message = "Password must be exactly 8 digits")
    private String password;
}
