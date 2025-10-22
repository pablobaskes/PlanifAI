package com.planifAI.diet_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionalGoalDTO {
    private UUID id;
    private UUID userId;
    private LocalDate startDate;
    private String mainGoal;
    private Double dailyCalories;
    private Double proteinGrams;
    private Double carbsGrams;
    private Double fatGrams;
}
