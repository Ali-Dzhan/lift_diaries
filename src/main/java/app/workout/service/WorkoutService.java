package app.workout.service;

import app.exercise.model.Exercise;
import app.exercise.repository.ExerciseRepository;
import app.progress.repository.ProgressRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.workout.model.Workout;
import app.workout.repository.WorkoutRepository;
import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMetadata;
import app.web.dto.ExerciseDTO;
import jakarta.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .orElseThrow(() -> new UsernameAlreadyExistException("User not found"));

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(workoutName);
        workout.setCompleted(isCompleted);
        workout.setCreatedOn(LocalDateTime.now());

        workout = workoutRepository.save(workout);

        workout.getExercises().addAll(exercises);

        workout = workoutRepository.save(workout);

        return workout;
    }

    public Workout getWorkoutById(UUID id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));
    }

    @Transactional
    public Workout repeatWorkout(UUID workoutId, AuthenticationMetadata authenticationMetadata) {

        Workout existingWorkout = getWorkoutById(workoutId);
        List<Exercise> existingExercises = existingWorkout.getExercises();
        if (existingExercises.isEmpty()) {
            throw new IllegalStateException("No exercises found for the repeated workout.");
        }

        Workout newWorkout = new Workout();
        newWorkout.setUser(existingWorkout.getUser());
        newWorkout.setName(existingWorkout.getName());
        newWorkout.setCompleted(false);
        newWorkout.setCreatedOn(LocalDateTime.now());

        newWorkout = workoutRepository.save(newWorkout);

        newWorkout.getExercises().addAll(existingExercises);

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

    @Transactional
    public int deleteWorkoutsBefore(LocalDate oneMonthAgo) {
        List<Workout> oldWorkouts = workoutRepository.findByCreatedOnBefore(oneMonthAgo.atStartOfDay());

        for (Workout workout : oldWorkouts) {
            progressRepository.deleteByWorkoutId(workout.getId());
            workout.getExercises().forEach(exercise -> exercise.setWorkout(null));
        }

        workoutRepository.deleteAll(oldWorkouts);
        return oldWorkouts.size();
    }
}
