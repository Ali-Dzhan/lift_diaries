package app.entity.workout.service;

import app.entity.exercise.model.Exercise;
import app.entity.exercise.repository.ExerciseRepository;
import app.entity.progress.repository.ProgressRepository;
import app.entity.user.model.User;
import app.entity.user.repository.UserRepository;
import app.entity.workout.model.Workout;
import app.entity.workout.repository.WorkoutRepository;
import app.exception.DomainException;
import app.web.dto.ExerciseDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    @Autowired
    public WorkoutService(WorkoutRepository workoutRepository,
                          ExerciseRepository exerciseRepository,
                          UserRepository userRepository,
                          ProgressRepository progressRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional
    public Workout createWorkout(String name, UUID userId, List<Exercise> exercises, boolean completed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .name(name)
                .user(user)
                .exercises(exercises)
                .completed(completed)
                .createdOn(LocalDateTime.now())
                .build();

        return workoutRepository.save(workout);
    }

    public Workout getWorkoutById(UUID id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));
    }

    @Transactional
    public Workout repeatWorkout(UUID workoutId, UUID userId) {
        Workout existingWorkout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new DomainException("Workout not found"));

        Workout newWorkout = new Workout();
        newWorkout.setUser(existingWorkout.getUser());
        newWorkout.setName(existingWorkout.getName());
        newWorkout.setCreatedOn(LocalDateTime.now());

        List<Exercise> newExercises = new ArrayList<>();
        for (Exercise exercise : existingWorkout.getExercises()) {
            Exercise newExercise = new Exercise();
            newExercise.setName(exercise.getName());
            newExercise.setDescription(exercise.getDescription());
            newExercise.setGifUrl(exercise.getGifUrl());
            newExercise.setSets(exercise.getSets());
            newExercise.setReps(exercise.getReps());

            if (exercise.getCategory() != null) {
                newExercise.setCategory(exercise.getCategory());
            }

            newExercises.add(newExercise);
        }

        for (Exercise newExercise : newExercises) {
            exerciseRepository.save(newExercise);
        }

        return newWorkout;
    }

    @Transactional
    public void deleteWorkoutAndProgress(UUID workoutId) {
        progressRepository.deleteByWorkoutId(workoutId);
        workoutRepository.deleteById(workoutId);
    }

    @Transactional
    public void markWorkoutAsCompleted(UUID id) {
        Workout workout = getWorkoutById(id);
        workout.setCompleted(true);
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
