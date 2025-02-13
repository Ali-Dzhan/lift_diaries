package app.web.mapper;

import app.entity.user.model.User;
import app.entity.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getCurrent")
    public ResponseEntity<User> getCurrentUser(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("loggedUserId");

        User user = userService.getById(userId);
        return ResponseEntity.ok(user);
    }
}
