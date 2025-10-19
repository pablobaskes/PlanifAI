package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    List<Ingredient> findByUserId(String userId);

    boolean existsByNameAndUserId(String name, String userId);
}
