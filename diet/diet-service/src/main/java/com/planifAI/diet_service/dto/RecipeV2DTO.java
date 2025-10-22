package com.planifAI.diet_service.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RecipeV2DTO {
    private UUID id;
    private String name;
    private Integer preparationTimeMin;
    private String instructions;
    private Integer servings;
    private String mealType;
    private String dietaryRestrictions;
    private List<RecipeFoodDTO> recipeFoods;
}
