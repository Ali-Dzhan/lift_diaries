package app.category.model;

import app.exercise.model.Exercise;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String imageUrl;

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    @JsonManagedReference
    private List<Exercise> exercises = new ArrayList<>();
}
