package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.MealDiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MealDiaryRepository extends JpaRepository<MealDiary, UUID> {
    List<MealDiary> findByUserId(UUID userId);
}
