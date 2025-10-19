package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {

    List<ShoppingList> findByUserId(String userId);
}
