package com.planifAI.diet_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressRecordDTO {
    private UUID id;
    private UUID userId;
    private LocalDate date;
    private Double weightKg;
    private Double bodyFatPercentage;
    private String bodyMeasurements;
    private Double waterIntakeMl;
}
