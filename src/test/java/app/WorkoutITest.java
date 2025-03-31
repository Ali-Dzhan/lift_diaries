package app;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import app.progress.model.Progress;
import app.progress.repository.ProgressRepository;
import app.progress.service.ProgressService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.workout.model.Workout;
import app.workout.repository.WorkoutRepository;
import app.workout.service.WorkoutService;
import app.security.AuthenticationMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static app.user.model.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WorkoutITest {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createWorkout_shouldPersistWorkoutAndLinkExercises() {
        // Given
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = TestBuilder.aRandomExercise(category);
        exercise.setCategory(category);
        exercise = exerciseRepository.save(exercise);

        AuthenticationMetadata auth = new AuthenticationMetadata
                (user.getId(), user.getUsername(), "12345678", USER, true);

        // When
        Workout created = workoutService.createWorkout
                ("Push Day", auth, List.of(exercise), true);

        // Then
        assertNotNull(created.getId());
        assertEquals("Push Day", created.getName());
        assertTrue(created.isCompleted());
        assertEquals(1, created.getExercises().size());
        assertEquals(exercise.getId(), created.getExercises().get(0).getId());
    }

    @Test
    void repeatWorkout_shouldDuplicateWorkoutCorrectly() {
        // Given
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = TestBuilder.aRandomExercise(category);
        exercise.setCategory(category);
        exercise = exerciseRepository.save(exercise);

        AuthenticationMetadata auth = new AuthenticationMetadata
                (user.getId(), user.getUsername(), "12345678", USER, true);
        Workout original = workoutService.createWorkout
                ("Leg Day", auth, List.of(exercise), true);

        // When
        Workout repeated = workoutService.repeatWorkout(original.getId(), auth);

        // Then
        assertNotEquals(original.getId(), repeated.getId());
        assertEquals(original.getName(), repeated.getName());
        assertFalse(repeated.isCompleted());
        assertEquals(original.getExercises().size(), repeated.getExercises().size());
    }

    @Test
    void deleteWorkoutAndProgress_shouldDeleteWorkoutAndLinkedProgress() {
        // Given
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = TestBuilder.aRandomExercise(category);
        exercise.setCategory(category);
        exercise = exerciseRepository.save(exercise);

        AuthenticationMetadata auth = new AuthenticationMetadata
                (user.getId(), user.getUsername(), "12345678", USER, true);
        Workout workout = workoutService.createWorkout
                ("Delete Me", auth, List.of(exercise), true);

        // Link a progress entry
        Progress progress = Progress.builder()
                .user(user)
                .workout(workout)
                .timestamp(LocalDate.now().atStartOfDay())
                .build();
        progressRepository.save(progress);

        UUID workoutId = workout.getId();

        // When
        workoutService.deleteWorkoutAndProgress(workoutId);

        // Then
        assertFalse(workoutRepository.findById(workoutId).isPresent());
    }

    @Test
    void createIncompleteWorkout_shouldHaveCompletedFalseAndNoProgress() {
        // Given
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = exerciseRepository.save(TestBuilder.aRandomExercise(category));
        AuthenticationMetadata auth = new AuthenticationMetadata(
                user.getId(), user.getUsername(), "12345678", USER, true
        );

        // When
        Workout workout = workoutService.createWorkout
                ("Unfinished Day", auth, List.of(exercise), false);

        // Then
        assertNotNull(workout.getId());
        assertEquals("Unfinished Day", workout.getName());
        assertFalse(workout.isCompleted());
        assertTrue(progressRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void getLastWorkoutMuscleGroup_shouldReturnCategoryName() {
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = exerciseRepository.save(TestBuilder.aRandomExercise(category));

        AuthenticationMetadata auth = new AuthenticationMetadata(
                user.getId(), user.getUsername(), "12345678", USER, true
        );
        Workout workout = workoutService.createWorkout
                ("Leg Day", auth, List.of(exercise), true);

        Progress progress = Progress.builder()
                .user(user)
                .workout(workout)
                .exercise(exercise)
                .timestamp(LocalDateTime.now())
                .build();

        progressRepository.save(progress);

        String result = progressService.getLastWorkoutMuscleGroup(user.getId());

        assertEquals(category.getName(), result);
    }

    @Test
    void calculateLongestWorkoutStreak_shouldReturnCorrectLongestStreak() {
        // Given
        User user = userRepository.save(TestBuilder.aRandomUser());
        Category category = categoryRepository.save(TestBuilder.aRandomCategory());
        Exercise exercise = exerciseRepository.save(TestBuilder.aRandomExercise(category));
        AuthenticationMetadata auth = new AuthenticationMetadata
                (user.getId(), user.getUsername(), "12345678", USER, true);

        progressRepository.deleteAll();

        LocalDateTime[] streakTimestamps = new LocalDateTime[] {
                LocalDateTime.now().minusDays(6),
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        };

        for (LocalDateTime timestamp : streakTimestamps) {
            Workout workout = workoutService.createWorkout
                    ("Workout", auth, List.of(exercise), false);
            Progress progress = Progress.builder()
                    .user(user)
                    .workout(workout)
                    .exercise(exercise)
                    .timestamp(timestamp)
                    .build();
            progressRepository.save(progress);
        }

        // When
        long longestStreak = progressService.calculateLongestStreak(user.getId());

        // Then
        assertEquals(3, longestStreak, "Expected longest workout streak to be 3");
    }

}
