package app.entity.exercise.service;

import app.entity.category.model.Category;
import app.entity.category.repository.CategoryRepository;
import app.entity.exercise.model.Exercise;
import app.entity.exercise.repository.ExerciseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, CategoryRepository categoryRepository) {
        this.exerciseRepository = exerciseRepository;
        this.categoryRepository = categoryRepository;
    }

    public Exercise createExercise(String name, String description, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + categoryId + " not found"));

        Exercise exercise = Exercise.builder()
                .name(name)
                .description(description)
                .category(category)
                .build();

        return exerciseRepository.save(exercise);
    }

    public Exercise getExerciseById(UUID id) {
        return exerciseRepository.findById(id).orElse(null);
    }

    public List<Exercise> getExercisesByCategory(UUID categoryId) {
        return exerciseRepository.findByCategoryId(categoryId);
    }

    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    public Exercise updateExercise(UUID id, String name, String description) {
        Exercise exercise = getExerciseById(id);

        if (exercise != null) {
            exercise.setName(name);
            exercise.setDescription(description);
            return exerciseRepository.save(exercise);
        }
        return null;
    }

    public void deleteExercise(UUID id) {
        exerciseRepository.deleteById(id);
    }
}
