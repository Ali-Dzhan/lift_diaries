package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.exercise.model.Exercise;
import app.progress.service.ProgressService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexPage_shouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getLoginPage_withoutError_shouldReturnLoginViewAndEmptyForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void getLoginPage_withErrorParam_shouldIncludeErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void getRegisterPage_shouldReturnRegisterViewAndEmptyForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRegisterUser_withValidData_shouldRedirectToLogin() throws Exception {
        // Given: valid register form
        RegisterRequest req = RegisterRequest.builder()
                .username("Pesho")
                .email("test@mail.com")
                .password("12345678")
                .build();

        when(userService.register(any())).thenReturn(User.builder().id(UUID.randomUUID()).build());

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .param("username", req.getUsername())
                .param("email", req.getEmail())
                .param("password", req.getPassword())
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getHomePage_shouldReturnHomeWithModel() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "user", "pass", UserRole.USER, true);

        User user = User.builder().id(userId).username("user").build();

        Category nextMuscleGroup = Category.builder()
                .name("Chest")
                .exercises(List.of(new Exercise(), new Exercise()))
                .build();

        when(userService.getById(userId)).thenReturn(user);
        when(progressService.calculateWorkoutStreak(userId)).thenReturn(5L);
        when(progressService.getTotalWorkouts(userId)).thenReturn(12);
        when(progressService.getLastWorkoutMuscleGroup(userId)).thenReturn("Back");
        when(progressService.getLastWorkoutDate(userId)).thenReturn("2025-03-20");
        when(progressService.getLastWorkoutExercises(userId)).thenReturn(List.of("Rows", "Pull-ups"));
        when(progressService.getMonthlyWorkoutCount(userId)).thenReturn(4);
        when(categoryService.getNextMuscleGroup(userId)).thenReturn(nextMuscleGroup);

        mockMvc.perform(get("/home").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "streak", "totalWorkouts",
                        "lastMuscleGroup", "lastWorkoutDate", "lastWorkoutExercises",
                        "monthlyWorkouts", "nextMuscleGroup", "suggestedExercises"));
    }

    @Test
    void getPrivacyPage_shouldReturnPrivacyViewWithUser() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "user", "pass", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(User.builder().id(userId).username("user").build());

        mockMvc.perform(get("/privacy").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("privacy"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getAboutPage_shouldReturnAboutViewWithUser() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (userId, "user", "pass", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(User.builder().id(userId).username("user").build());

        mockMvc.perform(get("/about").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(model().attributeExists("user"));
    }
}
