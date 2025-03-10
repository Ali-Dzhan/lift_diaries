package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationPreference;
import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(UserService userService,
                                  NotificationService notificationService) {

        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal AuthenticationMetadata auth) {
        UUID userId = auth.getUserId();
        User user = userService.getById(userId);

        NotificationPreference preference = notificationService.getNotificationPreference(userId);
        List<Notification> notificationHistory = notificationService.getNotificationHistory(userId);
        notificationHistory = notificationHistory.stream()
                .sorted(Comparator.comparing(Notification::getCreatedOn).reversed())
                .limit(5)
                .toList();

        ModelAndView model = new ModelAndView("notifications");
        model.addObject("user", user);
        model.addObject("notificationPreference", preference);
        model.addObject("notificationHistory", notificationHistory);
        return model;
    }

    @PutMapping("/user-preference")
    public String updateUserPreference(@RequestParam(name = "enabled") boolean enabled, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        notificationService.updateNotificationPreference(authenticationMetadata.getUserId(), enabled);

        return "redirect:/notifications";
    }
}
