package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findByUserId(String userId);

    List<Recipe> findByTagsContainingAndUserId(String tag, String userId);
}
