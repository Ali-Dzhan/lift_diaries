package app.web;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.exercise.model.Exercise;
import app.exercise.service.ExerciseService;
import app.progress.service.ProgressService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ExerciseDTO;
import app.web.dto.SelectedExercisesRequest;
import app.web.dto.WorkoutRequest;
import app.workout.model.Workout;
import app.workout.service.WorkoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutController.class)
class WorkoutControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WorkoutService workoutService;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private ProgressService progressService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID userId = UUID.randomUUID();

    private final AuthenticationMetadata principal = new AuthenticationMetadata
            (userId, "testUser", "123456", UserRole.USER, true);

    @Test
    void getWorkoutPage_shouldReturnCategoriesView() throws Exception {
        // Given
        User user = User.builder().id(userId).build();
        Category category = new Category();
        category.setName("Chest");

        when(userService.getById(userId)).thenReturn(user);
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        // When / Then
        mockMvc.perform(get("/workout").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("workout"))
                .andExpect(model().attributeExists("categories", "user"));
    }

    @Test
    void getExercisesByCategory_shouldReturnExerciseDTOs() throws Exception {
        Exercise exercise = new Exercise();
        exercise.setName("Push-up");
        exercise.setDescription("desc");
        exercise.setGifUrl("gif");
        exercise.setSets(3);
        exercise.setReps(10);

        when(exerciseService.getExercisesByCategory("Chest")).thenReturn(List.of(exercise));

        mockMvc.perform(get("/workout/exercise")
                        .param("categoryName", "Chest")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Push-up"))
                .andExpect(jsonPath("$[0].sets").value(3))
                .andExpect(jsonPath("$[0].reps").value(10));
    }

    @Test
    void selectExercises_shouldStoreSelectedExercises() throws Exception {
        List<UUID> exerciseIds = List.of(UUID.randomUUID());
        SelectedExercisesRequest request = new SelectedExercisesRequest();
        request.setSelectedExercises(exerciseIds);

        mockMvc.perform(post("/workout/selectExercises")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Exercises saved successfully."));

        verify(exerciseService).storeUserSelectedExercises(eq(userId), eq(exerciseIds));
    }

    @Test
    void selectExercises_withEmptyList_shouldReturnBadRequest() throws Exception {
        SelectedExercisesRequest request = new SelectedExercisesRequest();
        request.setSelectedExercises(List.of());

        mockMvc.perform(post("/workout/selectExercises")
                        .with(user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No exercises selected."));
    }

    @Test
    void startWorkout_shouldRenderStartWorkoutView() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId);
        Exercise exercise = new Exercise();

        User user = User.builder().id(userId).build();

        when(userService.getById(userId)).thenReturn(user);
        when(exerciseService.getUserSelectedExercises(userId)).thenReturn(exerciseIds);
        when(exerciseService.getExercisesByIds(exerciseIds)).thenReturn(List.of(exercise));

        mockMvc.perform(get("/workout/startWorkout").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("startWorkout"))
                .andExpect(model().attributeExists("sessionId", "exercises", "user"));
    }

    @Test
    void startWorkout_withoutSelectedExercises_shouldRedirect() throws Exception {
        User user = User.builder().id(userId).build();
        when(userService.getById(userId)).thenReturn(user);
        when(exerciseService.getUserSelectedExercises(userId)).thenReturn(List.of());

        mockMvc.perform(get("/workout/startWorkout").with(user(principal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout"));
    }

    @Test
    void completeWorkout_shouldReturnCompletedView() throws Exception {
        User user = User.builder().id(userId).build();
        when(userService.getById(userId)).thenReturn(user);

        mockMvc.perform(get("/workout/complete").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("completed"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void updateWorkout_shouldCallServiceAndRedirect() throws Exception {
        ExerciseDTO ex1 = new ExerciseDTO(
                UUID.randomUUID(), "Push", "desc", "gif", 3, 10);
        ExerciseDTO ex2 = new ExerciseDTO(
                UUID.randomUUID(), "Squat", "desc", "gif", 4, 12);

        mockMvc.perform(post("/workout/updateWorkout")
                        .with(csrf())
                        .with(user(principal))
                        .param("exercises[0].id", ex1.getId().toString())
                        .param("exercises[0].name", ex1.getName())
                        .param("exercises[0].description", ex1.getDescription())
                        .param("exercises[0].gifUrl", ex1.getGifUrl())
                        .param("exercises[0].sets", String.valueOf(ex1.getSets()))
                        .param("exercises[0].reps", String.valueOf(ex1.getReps()))
                        .param("exercises[1].id", ex2.getId().toString())
                        .param("exercises[1].name", ex2.getName())
                        .param("exercises[1].description", ex2.getDescription())
                        .param("exercises[1].gifUrl", ex2.getGifUrl())
                        .param("exercises[1].sets", String.valueOf(ex2.getSets()))
                        .param("exercises[1].reps", String.valueOf(ex2.getReps())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout"));

        verify(workoutService).updateWorkoutExercises(Mockito.anyList());
    }

}
