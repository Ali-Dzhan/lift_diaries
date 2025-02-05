package app.entity.user.property;

import app.entity.user.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "domain.user.properties")
public class UserProperties {

    @NotNull
    private UserRole defaultRole;

    private boolean activeByDefault;
}
