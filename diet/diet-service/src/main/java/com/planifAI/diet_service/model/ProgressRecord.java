package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "progress_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double weightKg;

    private Double bodyFatPercentage;

    private String bodyMeasurements; // Optional: could later be JSON for detailed body parts

    private Double waterIntakeMl;
}
