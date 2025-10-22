package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Food food;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false)
    private String userId;

    private boolean available;
}
