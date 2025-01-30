package app.user.property;

import app.user.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "domain.user.properties")
public class UserProperty {

    @NotNull
    private UserRole defaultRole;

    private boolean activeByDefault;
}
