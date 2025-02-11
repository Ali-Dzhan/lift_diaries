package app.web.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExerciseDTO {

    private UUID id;
    private String name;
    private String description;
    private String gifUrl;
    private int sets;
    private int reps;
}
