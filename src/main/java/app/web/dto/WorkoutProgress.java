package app.web.dto;

import app.entity.progress.model.Progress;
import app.entity.workout.model.Workout;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkoutProgress {

    private Workout workout;
    private List<Progress> progressList;
}
