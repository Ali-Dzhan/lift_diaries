package app.entity.workout.service;

import app.entity.exercise.model.Exercise;
import app.entity.exercise.repository.ExerciseRepository;
import app.entity.progress.repository.ProgressRepository;
import app.entity.user.model.User;
import app.entity.user.repository.UserRepository;
import app.entity.workout.model.Workout;
import app.entity.workout.repository.WorkoutRepository;
import app.exception.DomainException;
import app.security.AuthenticationMetadata;
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

    public Workout createWorkout(String workoutName, AuthenticationMetadata authenticationMetadata,
                                 List<Exercise> exercises, boolean isCompleted) {
        UUID userId = authenticationMetadata.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(workoutName);
        workout.setExercises(exercises);
        workout.setCompleted(isCompleted);
        workout.setCreatedOn(LocalDateTime.now());

        exercises.forEach(exercise -> exercise.setWorkout(workout));

        return workoutRepository.save(workout);
    }

    public Workout getWorkoutById(UUID id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));
    }

    @Transactional
    public Workout repeatWorkout(UUID workoutId, AuthenticationMetadata authenticationMetadata) {
        Workout existingWorkout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));

        List<Exercise> existingExercises = exerciseRepository.findAllByWorkoutId(workoutId);
        if (existingExercises.isEmpty()) {
            throw new IllegalStateException("No exercises found for the repeated workout.");
        }

        Workout newWorkout = new Workout();
        newWorkout.setUser(existingWorkout.getUser());
        newWorkout.setName(existingWorkout.getName() + " (Repeated)");
        newWorkout.setCreatedOn(LocalDateTime.now());

        List<Exercise> newExercises = new ArrayList<>();
        for (Exercise exercise : existingExercises) {
            Exercise newExercise = new Exercise();
            newExercise.setName(exercise.getName());
            newExercise.setDescription(exercise.getDescription());
            newExercise.setGifUrl(exercise.getGifUrl());
            newExercise.setSets(exercise.getSets());
            newExercise.setReps(exercise.getReps());
            newExercise.setCategory(exercise.getCategory());
            newExercise.setWorkout(newWorkout);
            newExercises.add(newExercise);
        }

        newWorkout.setExercises(newExercises);
        return workoutRepository.save(newWorkout);
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
