package app.web.dto;

import app.user.model.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Size(min = 8, message = "Username must be at least 8 characters")
    private String username;

    @Pattern(regexp = "\\d{8}", message = "Password must be exactly 8 digits")
    private String password;

    private Gender gender;
}
