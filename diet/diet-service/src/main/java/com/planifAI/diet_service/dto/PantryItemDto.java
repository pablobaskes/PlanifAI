package com.planifAI.diet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemDTO {
    private UUID id;
    private UUID ingredientId;
    private Double quantity;
    private boolean available;
    private String userId;
}
