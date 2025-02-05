package app.entity.progress.service;

import app.entity.exercise.model.Exercise;
import app.entity.exercise.repository.ExerciseRepository;
import app.entity.progress.model.Progress;
import app.entity.progress.repository.ProgressRepository;
import app.entity.user.model.User;
import app.entity.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ProgressService(ProgressRepository progressRepository,
                           UserRepository userRepository,
                           ExerciseRepository exerciseRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional
    public Progress logProgress(UUID userId, UUID exerciseId, double value, String unit, LocalDateTime timestamp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise with ID " + exerciseId + " not found"));

        Progress progress = Progress.builder()
                .user(user)
                .exercise(exercise)
                .value(value)
                .unit(unit)
                .timestamp(timestamp)
                .build();

        return progressRepository.save(progress);
    }

    public List<Progress> getProgressByUser(UUID userId) {
        return progressRepository.findByUserId(userId);
    }

    public List<Progress> getProgressByExercise(UUID exerciseId) {
        return progressRepository.findByExerciseId(exerciseId);
    }

    public List<Progress> getProgressByUserAndExercise(UUID userId, UUID exerciseId) {
        return progressRepository.findByUserIdAndExerciseId(userId, exerciseId);
    }

    public List<Progress> getProgressByUserAndDateRange(UUID userId, LocalDateTime start, LocalDateTime end) {
        return progressRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Transactional
    public void deleteProgress(UUID progressId) {
        Progress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Progress with ID " + progressId + " not found"));
        progressRepository.delete(progress);
    }
}
