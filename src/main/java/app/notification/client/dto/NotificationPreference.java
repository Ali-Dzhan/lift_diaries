package app.notification.client.dto;

import lombok.Data;

@Data
public class NotificationPreference {

    private boolean enabled;
    private String contactInfo;
}
