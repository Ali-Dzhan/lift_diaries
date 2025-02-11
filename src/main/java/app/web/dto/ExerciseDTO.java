package app.web.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExerciseDTO {

    private String name;
    private String description;

    private String gifUrl;

    private int sets;
    private int reps;
}
