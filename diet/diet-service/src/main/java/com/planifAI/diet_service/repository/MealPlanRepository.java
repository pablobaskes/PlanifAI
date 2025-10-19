package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    List<MealPlan> findByUserId(String userId);

    List<MealPlan> findByDateAndUserId(LocalDate date, String userId);
}
