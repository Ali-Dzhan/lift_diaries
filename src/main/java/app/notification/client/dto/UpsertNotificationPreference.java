package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertNotificationPreference {

    private UUID userId;
    private String contactInfo;
    private boolean notificationEnabled;
}
