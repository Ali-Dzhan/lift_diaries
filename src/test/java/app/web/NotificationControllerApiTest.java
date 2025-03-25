package app.web;

import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNotificationPage_shouldReturnNotificationsViewWithModel() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "user", "pass", UserRole.USER, true);

        NotificationPreference preference = new NotificationPreference();
        Notification notification = new Notification();

        when(userService.getById(userId)).thenReturn(User.builder().id(userId).build());
        when(notificationService.getNotificationPreference(userId)).thenReturn(preference);
        when(notificationService.getNotificationHistory(userId)).thenReturn(List.of(notification));

        mockMvc.perform(get("/notifications").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("user", "notificationPreference", "notificationHistory"));
    }

    @Test
    void putUpdateUserPreference_shouldRedirectToNotifications() throws Exception {
        // 1. Setup
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "user", "pass", UserRole.USER, true);

        // 2. Build Request
        MockHttpServletRequestBuilder request = put("/notifications/user-preference")
                .param("enabled", "true")
                .with(csrf())
                .with(user(principal));

        // 3. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService, times(1)).updateNotificationPreference(userId, true);
    }
}

