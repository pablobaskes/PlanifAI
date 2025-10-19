package com.planifAI.diet_service.service;

import com.planifAI.diet_service.dto.MealPlanDTO;
import com.planifAI.diet_service.mapper.MealPlanMapper;
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

    public MealPlanDTO create(MealPlanDTO dto) {
        MealPlan plan = mealPlanMapper.toEntity(dto);
        return mealPlanMapper.toDto(mealPlanRepository.save(plan));
    }

    public void delete(UUID id) {
        mealPlanRepository.deleteById(id);
    }
}
