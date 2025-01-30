package app.web.dto;

import app.user.model.Gender;
import app.user.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserInformation {

    private UUID id;
    private String username;
    private String email;
    private UserRole userRole;
    private Gender gender;
    private boolean isActive;
    private LocalDateTime createdOn;
}
