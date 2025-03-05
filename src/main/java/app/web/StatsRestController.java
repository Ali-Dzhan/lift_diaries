package app.web;

import app.entity.progress.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
public class StatsRestController {

    private final ProgressService progressService;

    public StatsRestController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Integer>> getStats(@PathVariable UUID userId) {
        int totalWorkouts = progressService.getTotalWorkouts(userId);
        long longestStreak = progressService.calculateWorkoutStreak(userId);
        int setsThisWeek = progressService.getSetsDoneThisWeek(userId);

        return ResponseEntity.ok(Map.of(
                "totalWorkouts", totalWorkouts,
                "longestStreak", (int) longestStreak,
                "setsThisWeek", setsThisWeek
        ));
    }
}
