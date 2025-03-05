package app.entity.progress.service;

import app.entity.exercise.repository.ExerciseRepository;
import app.entity.progress.model.Progress;
import app.entity.progress.repository.ProgressRepository;
import app.entity.user.repository.UserRepository;
import app.entity.workout.model.Workout;
import app.entity.workout.repository.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;

    @Autowired
    public ProgressService(ProgressRepository progressRepository,
                           UserRepository userRepository,
                           ExerciseRepository exerciseRepository, WorkoutRepository workoutRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
    }

    @Transactional
    public void saveWorkoutCompletion(UUID userId, UUID workoutId, UUID exerciseId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found for ID: " + workoutId));

        workout = workoutRepository.save(workout);

        Progress progress = Progress.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found")))
                .workout(workout)
                .exercise(exerciseId != null ? exerciseRepository.findById(exerciseId).orElse(null) : null)
                .timestamp(LocalDateTime.now())
                .build();

        progressRepository.save(progress);
    }

    public long calculateWorkoutStreak(UUID userId) {
        List<Progress> progresses = progressRepository
                .findRecentProgressByUser(userId, PageRequest.of(0, 30));
        if (progresses.isEmpty()) {
            return 0;
        }
        Set<LocalDate> uniqueWorkoutDays = new HashSet<>();

        for (Progress p : progresses) {
            uniqueWorkoutDays.add(p.getTimestamp().toLocalDate());
        }
        LocalDate today = LocalDate.now();
        long streak = 0;

        while (uniqueWorkoutDays.contains(today)) {
            streak++;
            today = today.minusDays(1);
        }
        return streak;
    }

    public List<Progress> getUserProgressSummary(UUID userId) {
        return progressRepository.findByUserId(userId);
    }

    public int getTotalWorkouts(UUID userId) {
        return (int) progressRepository.findByUserId(userId).stream()
                .map(Progress::getWorkout)
                .distinct()
                .count();
    }

    public String getLastWorkoutMuscleGroup(UUID userId) {
        List<Progress> progresses = progressRepository.findByUserId(userId);
        if (progresses.isEmpty()) return "N/A";

        return progresses.get(progresses.size() - 1).getExercise().getCategory().getName();
    }

    public String getLastWorkoutDate(UUID userId) {
        Optional<Progress> lastProgress = progressRepository.findByUserId(userId)
                .stream()
                .max(Comparator.comparing(Progress::getTimestamp));

        return lastProgress.map(progress -> progress.getTimestamp().toLocalDate().toString()).orElse("N/A");
    }

    public List<String> getLastWorkoutExercises(UUID userId) {
        List<Progress> progresses = progressRepository.findByUserId(userId);
        if (progresses.isEmpty()) return List.of("No recent workouts");

        progresses.sort(Comparator.comparing(Progress::getTimestamp).reversed());
        UUID lastWorkoutId = progresses.get(0).getWorkout().getId();

        return progresses.stream()
                .filter(p -> p.getWorkout().getId().equals(lastWorkoutId))
                .map(p -> p.getExercise().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    public int getMonthlyWorkoutCount(UUID userId) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return (int) progressRepository.findByUserId(userId).stream()
                .filter(progress -> progress.getTimestamp().isAfter(startOfMonth))
                .map(Progress::getWorkout)
                .distinct()
                .count();
    }

    public int getSetsDoneThisWeek(UUID userId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        return (int) progressRepository.findByUserId(userId).stream()
                .filter(progress -> progress.getTimestamp().isAfter(oneWeekAgo))
                .count();
    }
}
