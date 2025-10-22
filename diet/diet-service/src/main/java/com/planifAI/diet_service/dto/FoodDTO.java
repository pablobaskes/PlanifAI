package com.planifAI.diet_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FoodDTO {
    private UUID id;
    private String name;
    private Double caloriesPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;
    private String portionUnit;
}
