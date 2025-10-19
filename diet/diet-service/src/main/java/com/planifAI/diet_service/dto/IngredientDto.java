package com.planifAI.diet_service.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {
    private UUID id;
    private String name;
    private Double quantity;
    private String unit;
    private String userId;
}
