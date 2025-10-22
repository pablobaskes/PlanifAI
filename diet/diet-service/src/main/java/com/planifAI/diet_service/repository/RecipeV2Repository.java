package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.RecipeV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeV2Repository extends JpaRepository<RecipeV2, UUID> {
}
