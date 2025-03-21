package app.notification.service;

import app.exception.NotificationServiceFeignCallException;
import app.notification.client.NotificationClient;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.UpsertNotificationPreference;
import app.progress.service.ProgressService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${notifications.failure-message}")
    private String failureMessage;

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
            throw new NotificationServiceFeignCallException(failureMessage);
        }
    }

    public void saveNotificationPreference(UUID userId, boolean enabled, String email) {
        UpsertNotificationPreference pref = UpsertNotificationPreference.builder()
                .userId(userId)
                .notificationEnabled(enabled)
                .contactInfo(email)
                .build();

        try {
            ResponseEntity<Void> httpResponse = notificationClient.upsertNotificationPreference(pref);
            if (httpResponse == null || !httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notifications failed] Can't save user preference for user with id = [{}]", userId);
                throw new NotificationServiceFeignCallException("Failed to upsert user preference for userId=" + userId);
            }
        } catch (Exception e) {
            log.error("Unable to call notifications. Cause: {}", e.getMessage());
            throw new NotificationServiceFeignCallException("Error calling notification service for userId=" + userId);
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {
        try {
            ResponseEntity<NotificationPreference> response = notificationClient.getUserPreference(userId);

            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to retrieve preference for user [{}]", userId);
                throw new NotificationServiceFeignCallException("Unable to get preference for userId=" + userId);
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("Error retrieving preference for user [{}]: {}", userId, e.getMessage());
            throw new NotificationServiceFeignCallException("Notification service is temporarily unavailable. Please try again later.");
        }
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        try {
            ResponseEntity<List<Notification>> response = notificationClient.getNotificationHistory(userId);
            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to retrieve notification history for user [{}]", userId);
                throw new NotificationServiceFeignCallException(failureMessage);
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Unable to get notification history. Cause: {}", e.getMessage());
            throw new NotificationServiceFeignCallException(failureMessage);
        }
    }

    public void updateNotificationPreference(UUID userId, boolean enabled) {

        try {
            notificationClient.updateNotificationPreference(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [{}].", userId);
            throw new NotificationServiceFeignCallException(failureMessage);
        }
    }
}
