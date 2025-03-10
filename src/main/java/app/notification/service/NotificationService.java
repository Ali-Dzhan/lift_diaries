package app.notification.service;

import app.notification.client.NotificationClient;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.UpsertNotificationPreference;
import app.progress.service.ProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient notificationClient;
    private final ProgressService progressService;

    @Autowired
    public NotificationService(NotificationClient notificationClient, ProgressService progressService) {
        this.notificationClient = notificationClient;
        this.progressService = progressService;
    }

    public void checkAndSendWorkoutNotification(UUID userId) {
        log.info("Checking workout notification for user: {}", userId);

        long workoutStreak = progressService.calculateWorkoutStreak(userId);
        String message = (workoutStreak >= 3)
                ? "Rest day! Time to recover."
                : "Time to hit the gym! Keep the streak going.";

        NotificationRequest inAppRequest = NotificationRequest.builder()
                .userId(userId)
                .subject("Workout Alert")
                .body(message)
                .build();

        try {
            ResponseEntity<Void> response = notificationClient.sendNotification(inAppRequest);
            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to send in-app notification to user [{}]", userId);
            } else {
                log.info("In-app notification sent to user [{}]", userId);
            }
        } catch (Exception e) {
            log.warn("Error sending in-app notification to user [{}]: {}", userId, e.getMessage());
        }
    }

    public void saveNotificationPreference(UUID userId, boolean enabled, String email) {
        UpsertNotificationPreference pref = UpsertNotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(enabled)
                .contactInfo(email)
                .build();

        try {
            ResponseEntity<Void> response = notificationClient.upsertNotificationPreference(pref);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to save notification preference for user [{}]", userId);
            } else {
                log.info("Saved notification preference for user [{}]", userId);
            }
        } catch (Exception e) {
            log.error("Error saving notification preference for user [{}]: {}", userId, e.getMessage());
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {
        ResponseEntity<NotificationPreference> response = notificationClient.getUserPreference(userId);
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to retrieve preference for user [{}]", userId);
            return null;
        }
        return response.getBody();
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        ResponseEntity<List<Notification>> response = notificationClient.getNotificationHistory(userId);
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to retrieve notification history for user [{}]", userId);
            return List.of();
        }
        return response.getBody();
    }

    public void updateNotificationPreference(UUID userId, boolean enabled) {

        try {
            notificationClient.updateNotificationPreference(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [%s].".formatted(userId));
        }
    }
}
