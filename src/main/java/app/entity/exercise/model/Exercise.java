package app.entity.exercise.model;

import app.entity.category.model.Category;
import app.entity.workout.model.Workout;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    private String gifUrl;

    @Column
    private int sets;

    @Column
    private int reps;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    @JsonIgnore
    private Workout workout;
}
