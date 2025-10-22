package com.planifAI.diet_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RecipeFoodDTO {
    private UUID id;
    private UUID recipeId;
    private UUID foodId;
    private Double quantityGramsMl;
    private String notes;
}
