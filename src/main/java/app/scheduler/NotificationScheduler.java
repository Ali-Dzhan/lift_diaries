package app.scheduler;

import app.notification.service.NotificationService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationScheduler {

    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationScheduler(UserService userService,
                                 NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyWorkoutNotifications() {

        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {

            notificationService.checkAndSendWorkoutNotification(user.getId());
        }
    }
}
