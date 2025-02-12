package app.entity.workout.service;

import app.entity.exercise.model.Exercise;
import app.entity.exercise.repository.ExerciseRepository;
import app.entity.user.model.User;
import app.entity.user.repository.UserRepository;
import app.entity.workout.model.Workout;
import app.entity.workout.repository.WorkoutRepository;
import app.web.dto.ExerciseDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Autowired
    public WorkoutService(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Workout createWorkout(String name, UUID userId, List<Exercise> exercises) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Workout workout = Workout.builder()
                .name(name)
                .user(user)
                .exercises(exercises)
                .completed(false)
                .build();

        return workoutRepository.save(workout);
    }

    public Workout getWorkoutById(UUID id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));
    }

    public List<Workout> getWorkoutsByUser(UUID userId) {
        return workoutRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteWorkout(UUID id) {
        Workout workout = getWorkoutById(id);
        workoutRepository.delete(workout);
    }

    @Transactional
    public void markWorkoutAsCompleted(UUID id) {
        Workout workout = getWorkoutById(id);
        workout.setCompleted(true);
        workoutRepository.save(workout);
    }

    @Transactional
    public void markWorkoutAsNotCompleted(UUID id) {
        Workout workout = getWorkoutById(id);
        workout.setCompleted(false);
        workoutRepository.save(workout);
    }

    @Transactional
    public void updateWorkoutExercises(List<ExerciseDTO> updatedExercises) {
        for (ExerciseDTO dto : updatedExercises) {
            Exercise exercise = exerciseRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

            exercise.setSets(dto.getSets());
            exercise.setReps(dto.getReps());
            exerciseRepository.save(exercise);
        }
    }
}
