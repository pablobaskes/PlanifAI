package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {

    List<PantryItem> findByUserId(String userId);

    Optional<PantryItem> findByIngredientIdAndUserId(UUID ingredientId, String userId);
}
