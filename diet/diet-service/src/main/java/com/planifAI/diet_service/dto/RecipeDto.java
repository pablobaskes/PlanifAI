package com.planifAI.diet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private UUID id;
    private String name;
    private String instructions;
    private Set<String> tags;
    private List<UUID> ingredientIds;
}
