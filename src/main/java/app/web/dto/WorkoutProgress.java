package app.web.dto;

import app.progress.model.Progress;
import app.workout.model.Workout;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkoutProgress {

    private Workout workout;
    private List<Progress> progressList;

    public LocalDateTime getLatestTimestamp() {
        return progressList.stream()
                .map(Progress::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }
}
