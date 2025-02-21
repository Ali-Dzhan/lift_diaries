package app.entity.progress.repository;

import app.entity.progress.model.Progress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    List<Progress> findByUserId(UUID userId);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId ORDER BY p.timestamp DESC")
    List<Progress> findRecentProgressByUser(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Progress p WHERE p.workout.id = :workoutId")
    void deleteByWorkoutId(@Param("workoutId") UUID workoutId);
}
