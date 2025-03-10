package app.notification.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    private String subject;
    private String body;
    private LocalDateTime createdOn;
}
