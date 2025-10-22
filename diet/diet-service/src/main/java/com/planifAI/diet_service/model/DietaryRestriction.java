package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dietary_restriction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietaryRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String restrictionName; // e.g. "Vegan", "Lactose-Free", "Nut Allergy"
}
