package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, UUID> {

    List<ShoppingListItem> findByShoppingListId(UUID shoppingListId);

    List<ShoppingListItem> findByUserId(String userId);
}
