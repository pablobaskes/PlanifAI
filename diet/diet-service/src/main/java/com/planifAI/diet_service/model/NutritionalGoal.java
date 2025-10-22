package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "nutritional_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionalGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private String mainGoal;

    @Column(nullable = false)
    private Double dailyCalories;

    @Column(nullable = false)
    private Double proteinGrams;

    @Column(nullable = false)
    private Double carbsGrams;

    @Column(nullable = false)
    private Double fatGrams;
}
