package app.exercise.repository;

import app.category.model.Category;
import app.exercise.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByCategory(Category category);

    List<Exercise> findAllByWorkoutId(UUID workoutId);
}
