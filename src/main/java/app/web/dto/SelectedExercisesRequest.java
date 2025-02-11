package app.web.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Data
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelectedExercisesRequest {
    private List<UUID> selectedExercises;
}
