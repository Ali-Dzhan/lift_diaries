package app.exercise;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import app.exercise.service.ExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceUTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private UUID userId;
    private UUID sessionId;
    private List<UUID> exerciseIds;
    private List<Exercise> exerciseList;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        exerciseIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        Exercise exercise1 = new Exercise();
        Exercise exercise2 = new Exercise();
        exercise1.setId(exerciseIds.get(0));
        exercise2.setId(exerciseIds.get(1));
        exerciseList = List.of(exercise1, exercise2);
    }

    @Test
    void getExercisesByIds_ShouldReturnExercises_WhenValidIdsProvided() {
        when(exerciseRepository.findAllById(exerciseIds)).thenReturn(exerciseList);

        List<Exercise> result = exerciseService.getExercisesByIds(exerciseIds);

        assertThat(result).hasSize(2);
        verify(exerciseRepository, times(1)).findAllById(exerciseIds);
    }

    @Test
    void getExercisesByCategory_ShouldReturnExercises_WhenCategoryExists() {
        String categoryName = "Strength";
        Category category = new Category();
        category.setName(categoryName);

        when(categoryRepository.findByName(categoryName)).thenReturn(category);
        when(exerciseRepository.findByCategory(category)).thenReturn(exerciseList);

        List<Exercise> result = exerciseService.getExercisesByCategory(categoryName);

        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findByName(categoryName);
        verify(exerciseRepository, times(1)).findByCategory(category);
    }

    @Test
    void getExercisesByCategory_ShouldReturnEmptyList_WhenCategoryDoesNotExist() {
        String categoryName = "NonExistent";

        when(categoryRepository.findByName(categoryName)).thenReturn(null);

        List<Exercise> result = exerciseService.getExercisesByCategory(categoryName);

        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findByName(categoryName);
        verify(exerciseRepository, never()).findByCategory(any());
    }

    @Test
    void getAllExercises_ShouldReturnAllExercises() {
        when(exerciseRepository.findAll()).thenReturn(exerciseList);

        List<Exercise> result = exerciseService.getAllExercises();

        assertThat(result).hasSize(2);
        verify(exerciseRepository, times(1)).findAll();
    }

    @Test
    void storeUserSelectedExercises_ShouldStoreExercisesForUser() {
        exerciseService.storeUserSelectedExercises(userId, exerciseIds);

        List<UUID> result = exerciseService.getUserSelectedExercises(userId);

        assertThat(result).containsExactlyInAnyOrderElementsOf(exerciseIds);
    }

    @Test
    void getUserSelectedExercises_ShouldReturnStoredExercises() {
        exerciseService.storeUserSelectedExercises(userId, exerciseIds);

        List<UUID> result = exerciseService.getUserSelectedExercises(userId);

        assertThat(result).containsExactlyInAnyOrderElementsOf(exerciseIds);
    }

    @Test
    void getUserSelectedExercises_ShouldReturnEmptyList_WhenNoExercisesStored() {
        List<UUID> result = exerciseService.getUserSelectedExercises(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void clearUserSelectedExercises_ShouldRemoveExercisesForUser() {
        exerciseService.storeUserSelectedExercises(userId, exerciseIds);
        exerciseService.clearUserSelectedExercises(userId);

        List<UUID> result = exerciseService.getUserSelectedExercises(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void storeWorkoutSessionId_ShouldStoreSessionForUser() {
        exerciseService.storeWorkoutSessionId(userId, sessionId);

        UUID result = exerciseService.getWorkoutSessionId(userId);

        assertThat(result).isEqualTo(sessionId);
    }

    @Test
    void getWorkoutSessionId_ShouldReturnStoredSession() {
        exerciseService.storeWorkoutSessionId(userId, sessionId);

        UUID result = exerciseService.getWorkoutSessionId(userId);

        assertThat(result).isEqualTo(sessionId);
    }

    @Test
    void getWorkoutSessionId_ShouldReturnNull_WhenNoSessionStored() {
        UUID result = exerciseService.getWorkoutSessionId(userId);

        assertThat(result).isNull();
    }
}
