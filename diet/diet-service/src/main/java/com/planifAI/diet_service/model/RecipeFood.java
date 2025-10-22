package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "recipe_foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeFood {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecipeV2 recipe;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    private Double quantityGramsMl;
    private String notes;
}
