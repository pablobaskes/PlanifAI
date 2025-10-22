package com.planifAI.diet_service.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietaryRestrictionDTO {
    private UUID id;
    private UUID userId;
    private String restrictionName;
}
