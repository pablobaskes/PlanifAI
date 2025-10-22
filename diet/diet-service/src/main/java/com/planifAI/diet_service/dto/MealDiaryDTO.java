package com.planifAI.diet_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDiaryDTO {
    private UUID id;
    private UUID userId;
    private LocalDateTime dateTime;
    private String mealType;
    private Double totalCalories;
    private String mealMacronutrients;
}
