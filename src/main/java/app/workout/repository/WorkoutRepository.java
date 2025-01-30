package app.workout.repository;

import app.workout.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    List<Workout> findAllByUserIdOrderByWorkoutDateDesc(UUID userId);
}
