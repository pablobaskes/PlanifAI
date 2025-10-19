package com.planifAI.diet_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate date; // día del plan (puedes ampliar a rango semanal si luego lo necesitas)

    @Column(nullable = false)
    private String mealType; // desayuno, comida, cena...

    @ManyToOne(optional = false)
    private Recipe recipe;

    @Column(nullable = false)
    private String userId;
}
