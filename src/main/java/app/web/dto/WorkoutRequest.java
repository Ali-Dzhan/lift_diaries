package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRequest {

    private String workoutName;
    private UUID userId;
    private List<UUID> exerciseIds;
    private boolean completed;
}
