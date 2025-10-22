package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.RecipeFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeFoodRepository extends JpaRepository<RecipeFood, UUID> {
}
