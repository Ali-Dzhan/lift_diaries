package app.progress.service;

import app.exercise.repository.ExerciseRepository;
import app.progress.model.Progress;
import app.progress.repository.ProgressRepository;
import app.user.repository.UserRepository;
import app.workout.model.Workout;
import app.workout.repository.WorkoutRepository;
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

    public long calculateLongestStreak(UUID userId) {
        List<Progress> progresses = progressRepository.findByUserId(userId);

        List<LocalDate> uniqueDates = progresses.stream()
                .map(progress -> progress.getTimestamp().toLocalDate())
                .distinct()
                .sorted()
                .toList();
        if (uniqueDates.isEmpty()) {
            return 0;
        }
        long longestStreak = 1;
        long currentStreak = 1;
        LocalDate previousDate = uniqueDates.get(0);

        for (int i = 1; i < uniqueDates.size(); i++) {
            LocalDate currentDate = uniqueDates.get(i);
            if (currentDate.minusDays(1).equals(previousDate)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            longestStreak = Math.max(longestStreak, currentStreak);
            previousDate = currentDate;
        }

        return longestStreak;
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

        return progresses.stream()
                .max(Comparator.comparing(Progress::getTimestamp))
                .map(p -> p.getExercise().getCategory().getName())
                .orElse("N/A");
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

        List<Progress> sortedProgresses = new ArrayList<>(progresses);
        sortedProgresses.sort(Comparator.comparing(Progress::getTimestamp).reversed());
        UUID lastWorkoutId = sortedProgresses.get(0).getWorkout().getId();

        return sortedProgresses.stream()
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

        return progressRepository.findByUserId(userId).stream()
                .filter(progress -> progress.getTimestamp().isAfter(oneWeekAgo))
                .mapToInt(progress -> progress.getWorkout().getExercises().size())
                .sum();
    }
}
