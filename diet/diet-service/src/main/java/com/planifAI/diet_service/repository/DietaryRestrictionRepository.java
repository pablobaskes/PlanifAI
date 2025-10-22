package com.planifAI.diet_service.repository;

import com.planifAI.diet_service.model.DietaryRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DietaryRestrictionRepository extends JpaRepository<DietaryRestriction, UUID> {
    List<DietaryRestriction> findByUserId(UUID userId);
}
