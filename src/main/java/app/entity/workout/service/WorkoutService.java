package app.entity.workout.service;

import app.entity.user.model.User;
import app.entity.user.repository.UserRepository;
import app.entity.workout.model.Workout;
import app.entity.workout.repository.WorkoutRepository;
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
    private final UserRepository userRepository;

    @Autowired
    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Workout createWorkout(String name, String description, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        Workout workout = Workout.builder()
                .name(name)
                .description(description)
                .user(user)
                .build();

        return workoutRepository.save(workout);
    }

    public Workout getWorkoutById(UUID id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout with ID " + id + " not found"));
    }

    public List<Workout> getWorkoutsByUser(UUID userId) {
        return workoutRepository.findByUserId(userId);
    }

    public List<Workout> getAllWorkouts() {
        return workoutRepository.findAll();
    }

    @Transactional
    public Workout updateWorkout(UUID id, String name, String description) {
        Workout workout = getWorkoutById(id);
        workout.setName(name);
        workout.setDescription(description);
        return workoutRepository.save(workout);
    }

    @Transactional
    public void deleteWorkout(UUID id) {
        Workout workout = getWorkoutById(id);
        workoutRepository.delete(workout);
    }
}
