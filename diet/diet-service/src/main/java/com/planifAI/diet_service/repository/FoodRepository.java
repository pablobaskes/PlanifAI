package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {
}
