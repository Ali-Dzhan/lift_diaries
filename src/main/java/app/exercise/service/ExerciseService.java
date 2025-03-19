package app.exercise.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CategoryRepository categoryRepository;
    private final Map<UUID, List<UUID>> selectedExercisesMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> workoutSessionMap = new ConcurrentHashMap<>();

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, CategoryRepository categoryRepository) {
        this.exerciseRepository = exerciseRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Exercise> getExercisesByIds(List<UUID> exerciseIds) {
        return exerciseRepository.findAllById(exerciseIds);
    }

    public List<Exercise> getExercisesByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            return List.of();
        }
        return exerciseRepository.findByCategory(category);
    }

    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    public void storeUserSelectedExercises(UUID userId, List<UUID> exerciseIds) {
        selectedExercisesMap.put(userId, new ArrayList<>(exerciseIds));
    }

    public List<UUID> getUserSelectedExercises(UUID userId) {
        return selectedExercisesMap.getOrDefault(userId, Collections.emptyList());
    }

    public void clearUserSelectedExercises(UUID userId) {
        selectedExercisesMap.remove(userId);
    }

    public void storeWorkoutSessionId(UUID userId, UUID sessionId) {
        workoutSessionMap.put(userId, sessionId);
    }

    public UUID getWorkoutSessionId(UUID userId) {
        return workoutSessionMap.get(userId);
    }
}
