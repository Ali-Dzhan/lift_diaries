package app.workout;

import app.exception.UsernameAlreadyExistException;
import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import app.progress.repository.ProgressRepository;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.ExerciseDTO;
import app.workout.model.Workout;
import app.workout.repository.WorkoutRepository;
import app.workout.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceUTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private WorkoutService workoutService;

    private UUID userId;
    private User mockUser;
    private Workout mockWorkout;
    private Exercise mockExercise;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .build();

        mockWorkout = new Workout();
        mockWorkout.setId(UUID.randomUUID());
        mockWorkout.setName("Test Workout");
        mockWorkout.setCreatedOn(LocalDateTime.now());
        mockWorkout.setExercises(new ArrayList<>());

        mockExercise = new Exercise();
        mockExercise.setId(UUID.randomUUID());
        mockExercise.setName("Squats");
        mockExercise.setSets(3);
        mockExercise.setReps(10);
    }

    @Test
    void createWorkout_ShouldThrowIfUserNotFound() {
        // Given
        String workoutName = "Leg Day";
        AuthenticationMetadata auth = new AuthenticationMetadata
                (userId, "testUser", "pass", null, true);
        List<Exercise> exercises = Collections.singletonList(mockExercise);
        boolean isCompleted = false;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () ->
                workoutService.createWorkout(workoutName, auth, exercises, isCompleted));
        verify(workoutRepository, never()).save(any());
    }

    @Test
    void createWorkout_ShouldCreateWorkout() {
        // Given
        String workoutName = "Leg Day";
        AuthenticationMetadata auth = new AuthenticationMetadata
                (userId, "testUser", "pass", null, true);
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(mockExercise);
        boolean isCompleted = false;

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Workout result = workoutService.createWorkout(workoutName, auth, exercises, isCompleted);

        // Then
        assertNotNull(result);
        assertEquals("Leg Day", result.getName());
        assertFalse(result.isCompleted());
        assertThat(result.getExercises()).hasSize(1);
        verify(workoutRepository, times(2)).save(any(Workout.class));
    }

    @Test
    void getWorkoutById_ShouldReturnWorkout() {
        UUID workoutId = UUID.randomUUID();
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(mockWorkout));

        Workout result = workoutService.getWorkoutById(workoutId);

        assertNotNull(result);
        assertEquals(mockWorkout.getName(), result.getName());
    }

    @Test
    void getWorkoutById_ShouldThrowIfNotFound() {
        UUID workoutId = UUID.randomUUID();
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                workoutService.getWorkoutById(workoutId));
    }

    @Test
    void repeatWorkout_ShouldThrowIfWorkoutNotFound() {
        UUID workoutId = UUID.randomUUID();
        AuthenticationMetadata auth = new AuthenticationMetadata
                (userId, "testUser", "pass", null, true);

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                workoutService.repeatWorkout(workoutId, auth));
    }

    @Test
    void repeatWorkout_ShouldThrowIfNoExercises() {
        // Given
        mockWorkout.setExercises(Collections.emptyList());
        when(workoutRepository.findById(mockWorkout.getId()))
                .thenReturn(Optional.of(mockWorkout));

        AuthenticationMetadata auth = new AuthenticationMetadata
                (userId, "testUser", "pass", null, true);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                workoutService.repeatWorkout(mockWorkout.getId(), auth));
    }

    @Test
    void repeatWorkout_ShouldCreateNewWorkoutWithSameExercises() {
        // Given
        mockWorkout.getExercises().add(mockExercise);
        when(workoutRepository.findById(mockWorkout.getId()))
                .thenReturn(Optional.of(mockWorkout));
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationMetadata auth = new AuthenticationMetadata
                (userId, "testUser", "pass", null, true);

        // When
        Workout result = workoutService.repeatWorkout(mockWorkout.getId(), auth);

        // Then
        assertNotNull(result);
        assertEquals(mockWorkout.getName(), result.getName());
        assertThat(result.getExercises()).hasSize(1);
        assertFalse(result.isCompleted());
        verify(workoutRepository, times(2)).save(any(Workout.class));
    }

    @Test
    void deleteWorkoutAndProgress_ShouldDeleteProgressAndWorkout() {
        UUID workoutId = UUID.randomUUID();

        doNothing().when(progressRepository).deleteByWorkoutId(workoutId);
        doNothing().when(workoutRepository).deleteById(workoutId);

        workoutService.deleteWorkoutAndProgress(workoutId);

        verify(progressRepository).deleteByWorkoutId(workoutId);
        verify(workoutRepository).deleteById(workoutId);
    }

    @Test
    void markWorkoutAsCompleted_ShouldSetCompletedToTrue() {
        UUID workoutId = UUID.randomUUID();
        mockWorkout.setId(workoutId);
        mockWorkout.setCompleted(false);

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(mockWorkout));

        workoutService.markWorkoutAsCompleted(workoutId);

        assertTrue(mockWorkout.isCompleted());
        verify(workoutRepository).save(mockWorkout);
    }

    @Test
    void markWorkoutAsCompleted_ShouldThrowIfNotFound() {
        UUID workoutId = UUID.randomUUID();
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                workoutService.markWorkoutAsCompleted(workoutId));
    }

    @Test
    void updateWorkoutExercises_ShouldUpdateSetsAndReps() {
        // Given
        ExerciseDTO dto = new ExerciseDTO();
        dto.setId(mockExercise.getId());
        dto.setSets(5);
        dto.setReps(12);

        when(exerciseRepository.findById(mockExercise.getId()))
                .thenReturn(Optional.of(mockExercise));

        List<ExerciseDTO> updatedExercises = List.of(dto);

        // When
        workoutService.updateWorkoutExercises(updatedExercises);

        // Then
        assertEquals(5, mockExercise.getSets());
        assertEquals(12, mockExercise.getReps());
        verify(exerciseRepository).save(mockExercise);
    }

    @Test
    void updateWorkoutExercises_ShouldThrowIfExerciseNotFound() {
        ExerciseDTO dto = new ExerciseDTO();
        dto.setId(UUID.randomUUID());
        dto.setSets(5);
        dto.setReps(12);

        when(exerciseRepository.findById(dto.getId())).thenReturn(Optional.empty());

        List<ExerciseDTO> updatedExercises = List.of(dto);

        assertThrows(IllegalArgumentException.class, () ->
                workoutService.updateWorkoutExercises(updatedExercises));
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void deleteWorkoutsBefore_ShouldDeleteOldWorkoutsAndTheirProgress() {
        // Given
        LocalDate cutoffDate = LocalDate.of(2023, 1, 1);
        Workout oldWorkout1 = new Workout();
        oldWorkout1.setId(UUID.randomUUID());
        oldWorkout1.setCreatedOn(LocalDateTime.of(2022, 12, 31, 10, 0));
        oldWorkout1.setExercises(new ArrayList<>());

        Workout oldWorkout2 = new Workout();
        oldWorkout2.setId(UUID.randomUUID());
        oldWorkout2.setCreatedOn(LocalDateTime.of(2022, 11, 25, 10, 0));
        oldWorkout2.setExercises(new ArrayList<>());

        List<Workout> oldWorkouts = List.of(oldWorkout1, oldWorkout2);

        when(workoutRepository.findByCreatedOnBefore(any(LocalDateTime.class)))
                .thenReturn(oldWorkouts);

        doNothing().when(progressRepository).deleteByWorkoutId(any(UUID.class));
        doNothing().when(workoutRepository).deleteAll(oldWorkouts);

        // When
        int result = workoutService.deleteWorkoutsBefore(cutoffDate);

        // Then
        assertEquals(2, result);
        verify(progressRepository, times(2)).deleteByWorkoutId(any(UUID.class));
        verify(workoutRepository).deleteAll(oldWorkouts);
    }
}
