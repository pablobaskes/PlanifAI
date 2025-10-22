package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meal_diary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String mealType; // e.g. Breakfast, Lunch, Dinner

    @Column(nullable = false)
    private Double totalCalories;

    @Column(nullable = false)
    private String mealMacronutrients; // e.g. "P:30g, C:50g, F:10g"

    @ManyToOne(optional = false)
    private Recipe recipe;
}
