package app.entity.exercise.repository;

import app.entity.category.model.Category;
import app.entity.exercise.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByCategoryIn(List<Category> categories);

    List<Exercise> findByCategory(Category category);
}
