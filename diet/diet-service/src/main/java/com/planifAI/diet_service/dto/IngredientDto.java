package com.planifAI.diet_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDto {
    private Long id;
    private String name;
    private Double quantity;
    private String unit;
}
