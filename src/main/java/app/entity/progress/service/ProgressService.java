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

import java.time.LocalDateTime;
import java.util.List;
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
                .value(1)
                .unit("workout")
                .build();

        progressRepository.save(progress);
    }

    public long calculateWorkoutStreak(UUID userId) {
        List<Progress> progresses = progressRepository.findRecentProgressByUser(userId, PageRequest.of(0, 30));
        LocalDateTime today = LocalDateTime.now();
        long streak = 0;

        for (Progress p : progresses) {
            if (p.getTimestamp().toLocalDate().isEqual(today.toLocalDate())) {
                streak++;
                today = today.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public List<Progress> getUserProgressSummary(UUID userId) {
        return progressRepository.findByUserId(userId);
    }
}
