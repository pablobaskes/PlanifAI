package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.IngredientDTO;
import com.planifAI.diet_service.dto.MealPlanDTO;
import com.planifAI.diet_service.mapper.MealPlanMapper;
import com.planifAI.diet_service.model.Ingredient;
import com.planifAI.diet_service.model.MealPlan;
import com.planifAI.diet_service.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanMapper mealPlanMapper;

    public List<MealPlanDTO> findAll() {
        return mealPlanMapper.toDtoList(mealPlanRepository.findAll());
    }

    public MealPlanDTO findById(UUID id) {
        return mealPlanRepository.findById(id)
                .map(mealPlanMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
    }

    public MealPlanDTO create(MealPlanDTO dto) {
        MealPlan plan = mealPlanMapper.toEntity(dto);
        return mealPlanMapper.toDto(mealPlanRepository.save(plan));
    }

    public MealPlanDTO update(UUID id, MealPlanDTO dto) {
        MealPlan entity = mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        mealPlanMapper.updateEntityFromDto(dto, entity);
        return mealPlanMapper.toDto(mealPlanRepository.save(entity));
    }

    public void delete(UUID id) {
        mealPlanRepository.deleteById(id);
    }
}
