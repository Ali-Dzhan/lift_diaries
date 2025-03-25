package app.web;

import app.category.model.Category;
import app.progress.service.ProgressService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.workout.model.Workout;
import app.workout.service.WorkoutService;
import app.exercise.model.Exercise;
import app.exercise.service.ExerciseService;
import app.progress.model.Progress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
public class ProgressControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private WorkoutService workoutService;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private UserService userService;

    private final UUID userId = UUID.randomUUID();
    private final AuthenticationMetadata principal = new AuthenticationMetadata
            (userId, "testUser", "123456", UserRole.USER, true);

    @Test
    void viewProgress_shouldReturnProgressViewWithModelAttributes() throws Exception {
        User user = User.builder().id(userId).username("testUser").build();
        Workout workout = Workout.builder().id(UUID.randomUUID()).build();

        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());

        Category category = new Category();
        category.setName("Chest");
        exercise.setCategory(category);

        Progress progress = new Progress();
        progress.setWorkout(workout);
        progress.setExercise(exercise);

        when(userService.getById(userId)).thenReturn(user);
        when(progressService.getUserProgressSummary(userId)).thenReturn(List.of(progress));
        when(progressService.calculateWorkoutStreak(userId)).thenReturn(2L);

        mockMvc.perform(get("/progress").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("progress"))
                .andExpect(model().attributeExists("workoutProgressList", "streak", "totalWorkouts", "user"));
    }


    @Test
    void deleteWorkout_shouldCallServiceAndRedirect() throws Exception {
        UUID workoutId = UUID.randomUUID();

        mockMvc.perform(post("/progress/delete/{workoutId}", workoutId)
                        .with(csrf())
                        .with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/progress"));

        verify(workoutService).deleteWorkoutAndProgress(workoutId);
    }

    @Test
    void repeatWorkout_shouldCallServicesAndRedirectToStartWorkout() throws Exception {
        UUID workoutId = UUID.randomUUID();
        UUID newWorkoutId = UUID.randomUUID();
        Exercise ex1 = new Exercise();
        ex1.setId(UUID.randomUUID());

        Workout newWorkout = Workout.builder()
                .id(newWorkoutId)
                .exercises(List.of(ex1))
                .build();

        when(workoutService.repeatWorkout(eq(workoutId), any(AuthenticationMetadata.class))).thenReturn(newWorkout);

        mockMvc.perform(post("/progress/repeat/{workoutId}", workoutId)
                        .with(csrf())
                        .with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout/startWorkout?workoutId=" + newWorkoutId));

        verify(exerciseService).storeUserSelectedExercises(eq(userId), eq(List.of(ex1.getId())));
    }
}
