package app.progress;

import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import app.progress.model.Progress;
import app.progress.repository.ProgressRepository;
import app.progress.service.ProgressService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.workout.model.Workout;
import app.workout.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceUTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @InjectMocks
    private ProgressService progressService;

    private UUID userId;
    private UUID workoutId;
    private UUID exerciseId;
    private User user;
    private Workout workout;
    private Exercise exercise;
    private Progress progress;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workoutId = UUID.randomUUID();
        exerciseId = UUID.randomUUID();
        user = new User();
        workout = new Workout();
        workout.setId(workoutId);

        exercise = new Exercise();
        exercise.setName("Deadlift");

        progress = Progress.builder()
                .user(user)
                .workout(workout)
                .exercise(exercise)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void saveWorkoutCompletion_ShouldSaveProgress_WhenWorkoutExists() {
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        progressService.saveWorkoutCompletion(userId, workoutId, exerciseId);

        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    @Test
    void saveWorkoutCompletion_ShouldThrowException_WhenWorkoutNotFound() {
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> progressService.saveWorkoutCompletion(userId, workoutId, exerciseId));
    }

    @Test
    void calculateWorkoutStreak_ShouldReturnStreakCount() {
        List<Progress> progresses = List.of(
                Progress.builder().timestamp(LocalDateTime.now()).build(),
                Progress.builder().timestamp(LocalDateTime.now().minusDays(1)).build()
        );
        when(progressRepository.findRecentProgressByUser(eq(userId), any(PageRequest.class))).thenReturn(progresses);

        long streak = progressService.calculateWorkoutStreak(userId);
        assertThat(streak).isEqualTo(2);
    }

    @Test
    void getUserProgressSummary_ShouldReturnUserProgress() {
        List<Progress> progresses = List.of(progress);
        when(progressRepository.findByUserId(userId)).thenReturn(progresses);

        List<Progress> result = progressService.getUserProgressSummary(userId);
        assertThat(result).hasSize(1);
    }

    @Test
    void getTotalWorkouts_ShouldReturnCorrectCount() {
        List<Progress> progresses = List.of(progress, progress);
        when(progressRepository.findByUserId(userId)).thenReturn(progresses);

        int totalWorkouts = progressService.getTotalWorkouts(userId);
        assertThat(totalWorkouts).isEqualTo(1); // Workouts are distinct
    }

    @Test
    void getLastWorkoutDate_ShouldReturnLastWorkoutDate() {
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));

        String result = progressService.getLastWorkoutDate(userId);
        assertThat(result).isEqualTo(progress.getTimestamp().toLocalDate().toString());
    }

    @Test
    void getLastWorkoutExercises_ShouldReturnListOfExercises() {
        workout.setId(UUID.randomUUID());
        progress.setWorkout(workout);

        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));

        List<String> exercises = progressService.getLastWorkoutExercises(userId);
        assertThat(exercises).contains("Deadlift");
    }

    @Test
    void getMonthlyWorkoutCount_ShouldReturnCorrectCount() {
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));

        int count = progressService.getMonthlyWorkoutCount(userId);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getSetsDoneThisWeek_ShouldReturnCorrectSetCount() {
        // Given
        Workout workout = new Workout();
        workout.setId(UUID.randomUUID());
        Exercise exercise1 = new Exercise();
        exercise1.setId(UUID.randomUUID());
        Exercise exercise2 = new Exercise();
        exercise2.setId(UUID.randomUUID());

        workout.setExercises(List.of(exercise1, exercise2));

        Progress progress = Progress.builder()
                .user(user)
                .workout(workout)
                .timestamp(LocalDateTime.now())
                .build();

        when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));

        // When
        int sets = progressService.getSetsDoneThisWeek(userId);

        // Then
        assertThat(sets).isEqualTo(2);
    }
}
