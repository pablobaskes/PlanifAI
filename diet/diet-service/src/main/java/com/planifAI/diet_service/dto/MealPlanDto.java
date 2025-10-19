package com.planifAI.diet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDto {
    private UUID id;
    private LocalDate date;
    private String mealType;
    private UUID recipeId;
}
