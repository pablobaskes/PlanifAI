package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipes_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private Integer preparationTimeMin;
    private String instructions;
    private Integer servings;
    private String mealType; // Breakfast, Dinner, Snack, etc.
    private String dietaryRestrictions; // Vegan, Gluten-Free, etc.

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RecipeFood> recipeFoods;

    @ElementCollection
    private List<String> tags;
}
