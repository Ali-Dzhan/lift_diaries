package app.scheduler;

import app.entity.workout.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkoutCleanupScheduler {

    private final WorkoutService workoutService;

    @Autowired
    public WorkoutCleanupScheduler(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void deleteOldWorkouts() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

        int deleted = workoutService.deleteWorkoutsBefore(oneMonthAgo);
        System.out.println("🗑 Deleted " + deleted + " old workouts.");
    }
}
