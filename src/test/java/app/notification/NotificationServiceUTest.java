package app.notification;

import app.exception.NotificationServiceFeignCallException;
import app.notification.client.NotificationClient;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.client.dto.NotificationRequest;
import app.notification.service.NotificationService;
import app.progress.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;

    @Test
    void checkAndSendWorkoutNotification_ShouldSendNotification_WhenStreakLessThan3() {
        when(progressService.calculateWorkoutStreak(userId)).thenReturn(1L);
        when(notificationClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertThatCode(() -> notificationService.checkAndSendWorkoutNotification(userId))
                .doesNotThrowAnyException();

        verify(notificationClient).sendNotification(any());
    }

    @Test
    void checkAndSendWorkoutNotification_ShouldThrowException_OnFailure() {
        when(progressService.calculateWorkoutStreak(userId)).thenReturn(3L);
        when(notificationClient.sendNotification(any())).thenThrow(new RuntimeException("Connection error"));

        assertThatThrownBy(() -> notificationService.checkAndSendWorkoutNotification(userId))
                .isInstanceOf(NotificationServiceFeignCallException.class);
    }

    @Test
    void saveNotificationPreference_ShouldSucceed_WhenValid() {
        when(notificationClient.upsertNotificationPreference(any()))
                .thenReturn(ResponseEntity.ok().build());

        assertThatCode(() ->
                notificationService.saveNotificationPreference(userId, true, "user@mail.com")
        ).doesNotThrowAnyException();
    }

    @Test
    void getNotificationPreference_ShouldReturnPreference_WhenAvailable() {
        NotificationPreference pref = new NotificationPreference();

        when(notificationClient.getUserPreference(userId))
                .thenReturn(ResponseEntity.ok(pref));

        NotificationPreference result = notificationService.getNotificationPreference(userId);
        assertThat(result).isEqualTo(pref);
    }

    @Test
    void getNotificationHistory_ShouldReturnList_WhenSuccessful() {
        List<Notification> notifications = List.of(new Notification());
        when(notificationClient.getNotificationHistory(userId))
                .thenReturn(ResponseEntity.ok(notifications));

        List<Notification> result = notificationService.getNotificationHistory(userId);
        assertThat(result).isEqualTo(notifications);
    }

    @Test
    void updateNotificationPreference_ShouldSucceed_WhenClientWorks() {
        when(notificationClient.updateNotificationPreference(userId, true))
                .thenReturn(ResponseEntity.ok().build());

        assertThatCode(() ->
                notificationService.updateNotificationPreference(userId, true)
        ).doesNotThrowAnyException();
    }

    @Test
    void updateNotificationPreference_ShouldThrowException_WhenClientFails() {
        doThrow(new RuntimeException("Failed")).when(notificationClient)
                .updateNotificationPreference(userId, true);

        assertThatThrownBy(() ->
                notificationService.updateNotificationPreference(userId, true)
        ).isInstanceOf(NotificationServiceFeignCallException.class);
    }

    @Test
    void saveNotificationPreference_shouldNotThrowException_whenClientFails() {
        doThrow(new RuntimeException("Service Down"))
                .when(notificationClient).upsertNotificationPreference(any());

        notificationService.saveNotificationPreference(userId, true, "test@mail.com");
        verify(notificationClient).upsertNotificationPreference(any());
    }

    @Test
    void getNotificationPreference_shouldThrowException_whenResponseInvalid() {
        when(notificationClient.getUserPreference(userId)).thenReturn(ResponseEntity.internalServerError().build());

        assertThatThrownBy(() -> notificationService.getNotificationPreference(userId))
                .isInstanceOf(NotificationServiceFeignCallException.class)
                .hasMessageContaining("Unable to get preference");
    }

    @Test
    void getNotificationHistory_shouldThrowException_whenResponseInvalid() {
        when(notificationClient.getNotificationHistory(userId)).thenReturn(ResponseEntity.status(503).build());

        assertThatThrownBy(() -> notificationService.getNotificationHistory(userId))
                .isInstanceOf(NotificationServiceFeignCallException.class)
                .hasMessageContaining("Unable to get notification history");
    }
}
