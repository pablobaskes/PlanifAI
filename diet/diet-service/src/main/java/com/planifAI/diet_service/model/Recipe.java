package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String instructions;

    @ElementCollection
    private List<String> tags; // saludable, rápida, vegetariana, etc.

    @ManyToMany
    @JoinTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private String userId;
}
