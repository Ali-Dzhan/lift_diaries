package app.entity.progress.service;

import app.entity.exercise.repository.ExerciseRepository;
import app.entity.progress.model.Progress;
import app.entity.progress.repository.ProgressRepository;
import app.entity.user.repository.UserRepository;
import app.entity.workout.repository.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        if (!workoutRepository.existsById(workoutId)) {
            throw new RuntimeException("Workout not found for ID: " + workoutId);
        }

        Progress progress = Progress.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found")))
                .workout(workoutRepository.findById(workoutId).orElse(null))
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
}
