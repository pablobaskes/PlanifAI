package com.planifAI.diet_service.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDto {
    private UUID id;
    private String name;
    private Double quantity;
    private String unit;
    private String userId;
}
