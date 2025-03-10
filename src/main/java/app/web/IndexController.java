package app.web;

import app.progress.service.ProgressService;
import app.user.model.User;
import app.user.service.UserService;
import app.security.AuthenticationMetadata;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;
    private final ProgressService progressService;

    @Autowired
    public IndexController(UserService userService, ProgressService progressService) {
        this.userService = userService;
        this.progressService = progressService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "*Incorrect username or password!*");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        long streak = progressService.calculateWorkoutStreak(userId);
        int totalWorkouts = progressService.getTotalWorkouts(userId);
        String lastMuscleGroup = progressService.getLastWorkoutMuscleGroup(userId);
        String lastWorkoutDate = progressService.getLastWorkoutDate(userId);
        List<String> lastWorkoutExercises = progressService.getLastWorkoutExercises(userId);
        int monthlyWorkouts = progressService.getMonthlyWorkoutCount(userId);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("streak", streak);
        modelAndView.addObject("totalWorkouts", totalWorkouts);
        modelAndView.addObject("lastMuscleGroup", lastMuscleGroup);
        modelAndView.addObject("lastWorkoutDate", lastWorkoutDate);
        modelAndView.addObject("lastWorkoutExercises", lastWorkoutExercises);
        modelAndView.addObject("monthlyWorkouts", monthlyWorkouts);

        return modelAndView;
    }

    @GetMapping("/privacy")
    public ModelAndView getPrivacyPolicyPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView("privacy");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/about")
    public ModelAndView getAboutPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView("about");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
