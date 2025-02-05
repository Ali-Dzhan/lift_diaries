package app.entity.progress.repository;

import app.entity.progress.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    List<Progress> findByUserId(UUID userId);

    List<Progress> findByExerciseId(UUID exerciseId);

    List<Progress> findByUserIdAndExerciseId(UUID userId, UUID exerciseId);

    List<Progress> findByUserIdAndTimestampBetween(UUID userId, LocalDateTime start, LocalDateTime end);
}
